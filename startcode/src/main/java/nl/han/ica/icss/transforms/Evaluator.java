package nl.han.ica.icss.transforms;

import jdk.jfr.Percentage;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;

import java.util.*;

public class Evaluator implements Transform {

    private LinkedList<HashMap<String, Literal>> variableValues;

    public Evaluator() {
        variableValues = new LinkedList<>();
    }

    @Override
    public void apply(AST ast) {
        applyStyleSheet(ast.root);
    }

//Stylesheet
    private void applyStyleSheet(Stylesheet stylesheet) {
        variableValues.addFirst(new HashMap<>());
        List<ASTNode> removableNode = new ArrayList<>();
        
        for (ASTNode child : stylesheet.getChildren()) {
            if (child instanceof Stylerule) {
                applyStylerule((Stylerule) child);
            } else if (child instanceof VariableAssignment) {
                applyVariableAssignment((VariableAssignment) child);
                removableNode.add(child);
            }
        }

        for (ASTNode child: removableNode) {
            stylesheet.removeChild(child);
        }

        variableValues.removeFirst();
    }

//    Stylerule
    private void applyStylerule(Stylerule stylerule) {
        variableValues.addFirst(new HashMap<>());
        applyBody(stylerule.body);
        variableValues.removeFirst();        
    }

    private void applyBody(ArrayList<ASTNode> body) {
        List<ASTNode> nodesRemove = new ArrayList<>();
        List<ASTNode> nodesAdd = new ArrayList<>();

        for (ASTNode node  : body) {
            if (node instanceof Declaration) {
                applyDeclaration((Declaration) node);
            } else if (node instanceof VariableAssignment) {
                applyVariableAssignment((VariableAssignment) node);
                nodesRemove.add(node);
            } else if (node instanceof IfClause) {
                nodesAdd.addAll(applyIfClause((IfClause) node));
                nodesRemove.add(node);
            }
        }
        body.removeAll(nodesRemove);
        body.addAll(nodesAdd);
    }

    //    VariableAssignment
    private void applyVariableAssignment(VariableAssignment variableAssignment) {
        variableAssignment.expression = evaluateExpression(variableAssignment.expression);
        variableValues.getFirst().put(variableAssignment.name.name, (Literal) variableAssignment.expression);
    }

//    Declaration
    private void applyDeclaration(Declaration declaration) {
        declaration.expression = evaluateExpression(declaration.expression);
    }

//    Evaluate expression+subs
    private Literal evaluateExpression(Expression expression) {
        if (expression instanceof Literal) {
            return (Literal) expression;
        } else if (expression instanceof MultiplyOperation) {
            return evaluateMultiplyOperation((MultiplyOperation) expression);
        } else if (expression instanceof SubtractOperation) {
            return evaluateSubtractAddOperation((SubtractOperation) expression, true);
        } else if (expression instanceof AddOperation) {
            return evaluateSubtractAddOperation((AddOperation) expression, false);
        } else if (expression instanceof VariableReference) {
            return evaluateVariableReference((VariableReference) expression);
        }
        return null;
    }

    private Literal evaluateVariableReference(VariableReference variableReference) {
        for (Map<String, Literal> value : variableValues) {
            if (value.containsKey(variableReference.name)) {
                return value.get(variableReference.name);
            }
        }
        return null;
    }

    private Literal evaluateSubtractAddOperation(Operation operation, boolean isSubtract) {
        Literal left = evaluateExpression(operation.lhs);
        Literal right = evaluateExpression(operation.rhs);

        if (left instanceof PercentageLiteral && right instanceof PercentageLiteral) {
            if (isSubtract) {
                return new PercentageLiteral(((PercentageLiteral) left).value - ((PercentageLiteral) right).value);
            } else {
                return new PercentageLiteral(((PercentageLiteral) left).value + ((PercentageLiteral) right).value);
            }
        } else if (left instanceof PixelLiteral && right instanceof PixelLiteral) {
            if (isSubtract) {
                return new PixelLiteral(((PixelLiteral) left).value - ((PixelLiteral) right).value);
            } else {
                return new PixelLiteral(((PixelLiteral) left).value + ((PixelLiteral) right).value);
            }
        } else if (left instanceof ScalarLiteral && right instanceof ScalarLiteral) {
            if (isSubtract) {
                return new ScalarLiteral(((ScalarLiteral) left).value - ((ScalarLiteral) right).value);
            } else {
                return new ScalarLiteral(((ScalarLiteral) left).value + ((ScalarLiteral) right).value);
            }
        }
        return null;
    }

    private Literal evaluateMultiplyOperation(MultiplyOperation operation) {
        Literal left = evaluateExpression(operation.lhs);
        Literal right = evaluateExpression(operation.rhs);

//        All scalars right if only 1
        if (left instanceof ScalarLiteral && !(right instanceof ScalarLiteral)) {
            Literal temp = right;
            right= left;
            left = temp;
        }

        if (right instanceof ScalarLiteral) {
            if (left instanceof PixelLiteral) {
                return new PixelLiteral(((PixelLiteral) left).value * ((ScalarLiteral) right).value);
            } else if (left instanceof ScalarLiteral) {
                return new ScalarLiteral(((ScalarLiteral) left).value * ((ScalarLiteral) right).value);
            } else if (left instanceof PercentageLiteral) {
                return new PercentageLiteral(((PercentageLiteral) left).value * ((ScalarLiteral) right).value);
            }
        }

        return null;
    }

//    If else
    private List<ASTNode> applyIfClause(IfClause ifClause) {
        boolean isTrue = ((BoolLiteral) Objects.requireNonNull(evaluateExpression(ifClause.conditionalExpression))).value;
        if (isTrue) {
            applyBody(ifClause.body);
            return ifClause.body;
        } else if (ifClause.elseClause != null) {
            applyBody(ifClause.elseClause.body);
            return ifClause.elseClause.body;
        } else {
            return new ArrayList<>();
        }
    }

}
