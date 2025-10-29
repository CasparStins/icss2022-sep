package nl.han.ica.icss.transforms;

import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;

import java.util.HashMap;
import java.util.LinkedList;

public class Evaluator implements Transform {

    private LinkedList<HashMap<String, Literal>> variableValues;

    public Evaluator() {
        variableValues = new LinkedList<>();
    }

    @Override
    public void apply(AST ast) {
        evaluateStyleSheet(ast.root);
    }

    private void evaluateStyleSheet(Stylesheet stylesheet) {
        for (ASTNode child : stylesheet.getChildren()) {
            if (child instanceof Stylerule) {
                evaluateStylerule((Stylerule) child);
            }
        }
    }

    private void evaluateStylerule(Stylerule stylerule) {
        for(ASTNode child : stylerule.getChildren()) {
            if (child instanceof Declaration) {
                evaluateDeclaration((Declaration) child);
            }
        }
    }

    private void evaluateDeclaration(Declaration declaration) {
        Expression expression;
    }


}
