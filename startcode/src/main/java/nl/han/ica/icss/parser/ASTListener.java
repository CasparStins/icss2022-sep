package nl.han.ica.icss.parser;

import nl.han.ica.datastructures.HANStack;
import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;

/**
 * This class extracts the ICSS Abstract Syntax Tree from the Antlr Parse tree.
 */
public class ASTListener extends ICSSBaseListener {
	
	//Accumulator attributes:
	private AST ast;

	//Use this to keep track of the parent nodes when recursively traversing the ast
	private IHANStack<ASTNode> currentContainer;

	public ASTListener() {
		ast = new AST();
		currentContainer = new HANStack<>();
	}
    public AST getAST() {
        return ast;
    }

//	Stylesheet
	@Override
	public void enterStylesheet(ICSSParser.StylesheetContext ctx) {
		currentContainer.push(new Stylesheet());
	}

	@Override
	public void exitStylesheet(ICSSParser.StylesheetContext ctx) {
		Stylesheet styleSheet = (Stylesheet) currentContainer.pop();
		ast.setRoot(styleSheet);
	}

//	StyleRule
	@Override
	public void enterStylerule(ICSSParser.StyleruleContext ctx) {
		currentContainer.push(new Stylerule());
	}

	@Override
	public void exitStylerule(ICSSParser.StyleruleContext ctx) {
		Stylerule stylerule = (Stylerule) currentContainer.pop();
		currentContainer.peek().addChild(stylerule);
	}

//	Selector
	@Override
	public void exitSelector(ICSSParser.SelectorContext ctx) {
		Selector selector;
		if (ctx.LOWER_IDENT() != null) {
			selector = new TagSelector(ctx.LOWER_IDENT().getText());
		} else if (ctx.ID_IDENT() != null) {
			selector = new IdSelector(ctx.ID_IDENT().getText());
		} else {
			selector = new ClassSelector(ctx.CLASS_IDENT().getText());
		}
		currentContainer.peek().addChild(selector);
	}

//	Declaration
	@Override
	public void enterDeclaration(ICSSParser.DeclarationContext ctx) {
		currentContainer.push(new Declaration());
	}

//	??
	@Override
	public void exitDeclaration(ICSSParser.DeclarationContext ctx) {
		Declaration declaration = (Declaration) currentContainer.pop();
		currentContainer.peek().addChild(declaration);
	}

// Literal/Value
	@Override
	public void exitValue(ICSSParser.ValueContext ctx) {
		Expression expression;

		if (ctx.COLOR() != null) {
			expression = new ColorLiteral(ctx.COLOR().getText());
		} else if (ctx.PERCENTAGE() != null) {
			expression = new PercentageLiteral(ctx.PERCENTAGE().getText());
		} else {
			expression = new PixelLiteral(ctx.PIXELSIZE().getText());
		}

		currentContainer.peek().addChild(expression);
	}
}