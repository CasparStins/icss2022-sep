package nl.han.ica.icss.checker;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;


public class Checker {

    private LinkedList<HashMap<String, ExpressionType>> variableTypes;

    public void check(AST ast) {
        variableTypes = new LinkedList<>();
        checkStylesheet(ast.root);
    }

    private void checkStylesheet(Stylesheet stylesheet) {
        variableTypes.addFirst(new HashMap<>());

        for (ASTNode child : stylesheet.getChildren()) {
            if (child instanceof Stylerule) {
                checkStylerule((Stylerule) child);
            } else if (child instanceof VariableAssignment) {
                checkVariableAssignment((VariableAssignment) child);
            }
        }

        variableTypes.removeFirst();
    }

    private void checkVariableAssignment(VariableAssignment variableAssignment) {
        ExpressionType expressionType = checkExpression(variableAssignment.expression);
        variableTypes.getFirst().put(variableAssignment.name.name, expressionType);
    }

    private void checkStylerule(Stylerule stylerule) {
        variableTypes.addFirst(new HashMap<>());

        for (ASTNode child: stylerule.getChildren()) {
            if (child instanceof Declaration) {
                checkDeclaration((Declaration) child);
            } else if (child instanceof VariableAssignment) {
                checkVariableAssignment((VariableAssignment) child);
            } else if (child instanceof IfClause) {
                checkIfClause((IfClause) child);
            }
        }

        variableTypes.removeFirst();
    }

    private void checkIfClause(IfClause ifClause) {
        if (checkExpression(ifClause.conditionalExpression) != ExpressionType.BOOL) {
            ifClause.setError("Not a boolean");
        }

        checkBodyClause(ifClause.body);

        if (ifClause.elseClause != null) {
            checkElseClause(ifClause.elseClause);
        }
    }

    private void checkBodyClause(ArrayList<ASTNode> body) {
        variableTypes.addFirst(new HashMap<>());
        for (ASTNode node : body) {
            if (node instanceof VariableAssignment) {
                checkVariableAssignment((VariableAssignment) node);
            } else if (node instanceof IfClause) {
                checkIfClause((IfClause) node);
            } else if (node instanceof Declaration) {
                checkDeclaration((Declaration) node);
            }
        }
        variableTypes.removeFirst();
    }

    private void checkElseClause(ElseClause elseClause) {
        variableTypes.addFirst(new HashMap<>());
        for (ASTNode node : elseClause.body) {
            if (node instanceof Declaration) {
                checkDeclaration((Declaration) node);
            } else if (node instanceof VariableAssignment) {
                checkVariableAssignment((VariableAssignment) node);
            } else if (node instanceof IfClause) {
                checkIfClause((IfClause) node);
            }
        }
        variableTypes.removeFirst();
    }

    private void checkDeclaration(Declaration declaration) {
        ExpressionType expressionType = checkExpression(declaration.expression);

        switch (declaration.property.name) {
            case "width":
            case "height":
                if (expressionType != ExpressionType.PIXEL && expressionType != ExpressionType.PERCENTAGE) {
                    declaration.setError("Bij width en height moet er een pixel of percentage mee gegeven wordne.");
                } break;

            case "color":
            case "background-color":
                if (expressionType != ExpressionType.COLOR) {
                    declaration.setError("Bij kleur moet er een kleur meegegeven worden.");
                } break;

            default:
                declaration.setError("Geef een geldig property mee.");
                break;
        }
    }

    private ExpressionType checkExpression(Expression expression) {
        if (expression instanceof ColorLiteral) {
            return ExpressionType.COLOR;
        } else if (expression instanceof PixelLiteral) {
            return ExpressionType.PIXEL;
        } else if (expression instanceof PercentageLiteral) {
            return ExpressionType.PERCENTAGE;
        } else if (expression instanceof ScalarLiteral) {
            return ExpressionType.SCALAR;
        } else if (expression instanceof BoolLiteral) {
            return ExpressionType.BOOL;
        } else if (expression instanceof VariableReference) {
            return checkVariableReference((VariableReference) expression);
        } else if (expression instanceof Operation) {
            return checkOperation((Operation) expression);
        }
        expression.setError("Not a valid expression");
        return ExpressionType.UNDEFINED;
    }

    private ExpressionType checkOperation(Operation operation) {
        ExpressionType type1 = checkExpression(operation.lhs);
        ExpressionType type2 = checkExpression(operation.rhs);

//        Make sure no color or bool
        if (type1 == ExpressionType.COLOR || type1 == ExpressionType.BOOL ||
                type2 == ExpressionType.COLOR || type2 == ExpressionType.BOOL) {
            operation.setError("Not a color operation");
            return ExpressionType.UNDEFINED;
        }

//        Check if they are the same expression and return
        if (operation instanceof AddOperation || operation instanceof SubtractOperation) {
            if (type1 == type2 &&
                    (type1 == ExpressionType.PIXEL || type1 == ExpressionType.PERCENTAGE || type1 == ExpressionType.SCALAR)) {
                return type1;
            } else {
                operation.setError("Not a same type");
                return ExpressionType.UNDEFINED;
            }
        }

//        Check if at least one is scalar
        boolean isScalar1 = type1 == ExpressionType.SCALAR;
        boolean isScalar2 = type2 == ExpressionType.SCALAR;

        if (!isScalar1 && !isScalar2) {
            operation.setError("Not a scalar operation included");
            return ExpressionType.UNDEFINED;
        }

//        Return the right expression with multi
        if (operation instanceof MultiplyOperation) {
            if (type1 == ExpressionType.SCALAR && type2 == ExpressionType.SCALAR) {
                return ExpressionType.SCALAR;
            } else {
                if (type1 != ExpressionType.SCALAR) {
                    return type1;
                } else {
                    return type2;
                }
            }
        }
        operation.setError("Not a defined operation");
        return ExpressionType.UNDEFINED;
    }

    private ExpressionType checkVariableReference(VariableReference variableReference) {
        for (HashMap<String, ExpressionType> currentScope: variableTypes) {
            if (currentScope.containsKey(variableReference.name)) {
                return currentScope.get(variableReference.name);
            }
        }
        variableReference.setError("Variable reference '" + variableReference.name + "' not found.");
        return ExpressionType.UNDEFINED;
    }
}
