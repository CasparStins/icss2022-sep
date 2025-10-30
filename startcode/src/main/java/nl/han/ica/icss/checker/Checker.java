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
//                    error
                } break;

            case "color":
            case "background-color":
                if (expressionType != ExpressionType.COLOR) {
//                    error
                } break;

            default:
//                error
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

        return ExpressionType.UNDEFINED;
    }

    private ExpressionType checkOperation(Operation operation) {
        ExpressionType type1 = checkExpression(operation.lhs);
        ExpressionType type2 = checkExpression(operation.rhs);

        if (type1 == ExpressionType.COLOR || type1 == ExpressionType.BOOL ||
                type2 == ExpressionType.COLOR || type2 == ExpressionType.BOOL) {
            return ExpressionType.UNDEFINED;
        }

        if (operation instanceof AddOperation || operation instanceof SubtractOperation) {
            if (type1 == type2) {
                return type1;
            } else if (type1 == ExpressionType.SCALAR) {
                return type2;
            } else if (type2 == ExpressionType.SCALAR) {
                return type1;
            } else {
                return ExpressionType.UNDEFINED;
            }
        }

        if (operation instanceof MultiplyOperation) {
            if (type1 == ExpressionType.SCALAR && type2 == ExpressionType.SCALAR) {
                return ExpressionType.SCALAR;
            } else if (type1 == ExpressionType.SCALAR || type2 == ExpressionType.SCALAR) {
                if (type1 != ExpressionType.SCALAR) {
                    return type1;
                } else {
                    return type2;
                }
            }
        }

        return ExpressionType.UNDEFINED;
    }

    private ExpressionType checkVariableReference(VariableReference variableReference) {
        for (HashMap<String, ExpressionType> currentScope: variableTypes) {
            if (currentScope.containsKey(variableReference.name)) {
                return currentScope.get(variableReference.name);
            }
        }
        return ExpressionType.UNDEFINED;
    }
}
