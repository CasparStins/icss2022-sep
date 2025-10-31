package nl.han.ica.icss.generator;


import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;

public class Generator {

	public String generate(AST ast) {
        StringBuilder string = new StringBuilder();

		for (ASTNode node : ast.root.getChildren()) {
			Stylerule stylerule = (Stylerule) node;

//		Selector
			string.append(stylerule.selectors.get(0).toString())
					.append(" {\n");


//		Declarations
			for (ASTNode child : stylerule.body) {
				if (child instanceof Declaration) {
					Declaration declaration = (Declaration) child;

					string.append("  ")
							.append(declaration.property.name)
							.append(": ")
							.append(expressionToString(declaration.expression))
							.append(";\n");

				}
			}
			string.append("}\n\n");
		}
		return string.toString();
	}

	private String expressionToString(Expression expression) {
		if (expression instanceof PixelLiteral) {
			return ((PixelLiteral) expression).value + "px";
		} else if (expression instanceof ColorLiteral) {
			return ((ColorLiteral) expression).value;
		} else if (expression instanceof PercentageLiteral) {
			return ((PercentageLiteral) expression).value + "%";
		} else if (expression instanceof ScalarLiteral) {
			return String.valueOf(((ScalarLiteral) expression).value);
		}
		return "";
	}


}
