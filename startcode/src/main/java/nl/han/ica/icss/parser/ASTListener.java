package nl.han.ica.icss.parser;

import nl.han.ica.datastructures.HANStack;
import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;
import nl.han.ica.icss.ast.literals.*;


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


	@Override
	public void exitDeclaration(ICSSParser.DeclarationContext ctx) {
		Declaration declaration = (Declaration) currentContainer.pop();
		declaration.property = new PropertyName(ctx.LOWER_IDENT().getText());
		currentContainer.peek().addChild(declaration);
	}

	// VariableAssignment
	@Override
	public void enterVariableAssignment(ICSSParser.VariableAssignmentContext ctx) {
		VariableAssignment variableAssignment = new VariableAssignment();
		variableAssignment.name = new VariableReference(ctx.CAPITAL_IDENT().getText());
		currentContainer.push(variableAssignment);
	}

	@Override
	public void exitVariableAssignment(ICSSParser.VariableAssignmentContext ctx) {
		VariableAssignment variableAssignment = (VariableAssignment) currentContainer.pop();
		currentContainer.peek().addChild(variableAssignment);
	}


	//	Expressions
	@Override
	public void enterExpression(ICSSParser.ExpressionContext ctx) {
		if (ctx.MUL() != null) {
			currentContainer.push(new MultiplyOperation());
		} else if (ctx.PLUS() != null) {
			currentContainer.push(new AddOperation());
		} else if (ctx.MIN() != null) {
			currentContainer.push(new SubtractOperation());
		}
	}

	@Override
	public void exitExpression(ICSSParser.ExpressionContext ctx) {
		if (ctx.MUL() != null || ctx.PLUS() != null || ctx.MIN() != null) {
			Operation operation = (Operation) currentContainer.pop();
			currentContainer.peek().addChild(operation);
		} else if (ctx.CAPITAL_IDENT() != null) {
			currentContainer.peek().addChild(new VariableReference(ctx.CAPITAL_IDENT().getText()));
		}
	}

//	Literals
	@Override
	public void exitLiteral(ICSSParser.LiteralContext ctx) {
		Literal literal;

		if (ctx.COLOR() != null) {
			literal = new ColorLiteral(ctx.COLOR().getText());
		} else if (ctx.PIXELSIZE() != null) {
			literal = new PixelLiteral(ctx.PIXELSIZE().getText());
		} else if (ctx.PERCENTAGE() != null) {
			literal = new PercentageLiteral(ctx.PERCENTAGE().getText());
		} else if (ctx.SCALAR() != null) {
			literal = new ScalarLiteral(ctx.SCALAR().getText());
		} else if (ctx.TRUE() != null) {
			literal = new BoolLiteral("TRUE");
		} else {
			literal = new BoolLiteral("FALSE");
		}

		currentContainer.peek().addChild(literal);
	}

//	If
	@Override
	public void enterIfClause(ICSSParser.IfClauseContext ctx) {
		currentContainer.push(new IfClause());
	}

	@Override
	public void exitIfClause(ICSSParser.IfClauseContext ctx) {
		IfClause ifClause = (IfClause) currentContainer.pop();
		currentContainer.peek().addChild(ifClause);
	}

//	Else
	@Override
	public void enterElseClause(ICSSParser.ElseClauseContext ctx) {
		currentContainer.push(new ElseClause());
	}

	@Override
	public void exitElseClause(ICSSParser.ElseClauseContext ctx) {
		ElseClause elseClause = (ElseClause) currentContainer.pop();
		currentContainer.peek().addChild(elseClause);
	}



}