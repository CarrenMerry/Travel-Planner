package com.travelplanner.ds;

import java.util.ArrayList;
import java.util.List;

/**
 * Stack-based arithmetic expression evaluator.
 * Uses CustomStack for both operands and operators.
 * Supports +, -, *, /, parentheses (), floating-point numbers, and operator precedence.
 * Provides validation against division by zero, mismatched parentheses, and syntax errors.
 */
public class ExpressionEvaluator {

    public static class StepTrace {
        public String token;
        public String action;
        public List<Double> operandStack;
        public List<String> operatorStack;

        public StepTrace(String token, String action, List<Double> operandStack, List<String> operatorStack) {
            this.token = token;
            this.action = action;
            this.operandStack = operandStack;
            this.operatorStack = operatorStack;
        }
    }

    public static class EvaluationResult {
        private boolean success;
        private double result;
        private String errorMessage;
        private List<StepTrace> traces = new ArrayList<>();

        public EvaluationResult(double result, List<StepTrace> traces) {
            this.success = true;
            this.result = result;
            this.traces = traces;
        }

        public EvaluationResult(String errorMessage) {
            this.success = false;
            this.errorMessage = errorMessage;
        }

        public boolean isSuccess() {
            return success;
        }

        public double getResult() {
            return result;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public List<StepTrace> getTraces() {
            return traces;
        }
    }

    /**
     * Evaluates a mathematical expression string using two CustomStacks.
     * Time Complexity: O(n) where n is the length of the expression.
     * Space Complexity: O(n) for the stacks.
     */
    public static EvaluationResult evaluate(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return new EvaluationResult("Expression cannot be empty.");
        }

        CustomStack<Double> values = new CustomStack<>();
        CustomStack<Character> ops = new CustomStack<>();
        List<StepTrace> traces = new ArrayList<>();

        String cleanExpr = expression.replaceAll("\\s+", "");
        char[] tokens = cleanExpr.toCharArray();
        int n = tokens.length;

        // Check for mismatched parentheses
        int parenBalance = 0;
        for (char c : tokens) {
            if (c == '(') parenBalance++;
            if (c == ')') parenBalance--;
            if (parenBalance < 0) {
                return new EvaluationResult("Mismatched parentheses: unexpected closing parenthesis ')'.");
            }
        }
        if (parenBalance != 0) {
            return new EvaluationResult("Mismatched parentheses: unclosed opening parenthesis '('.");
        }

        for (int i = 0; i < n; i++) {
            char c = tokens[i];

            // If token is a number or decimal point
            // Also handle unary minus if at beginning or after '('
            boolean isUnaryMinus = (c == '-') && (i == 0 || tokens[i - 1] == '(');
            if (Character.isDigit(c) || c == '.' || isUnaryMinus) {
                StringBuilder sb = new StringBuilder();
                if (isUnaryMinus) {
                    sb.append('-');
                    i++;
                    if (i >= n || (!Character.isDigit(tokens[i]) && tokens[i] != '.')) {
                        return new EvaluationResult("Invalid syntax after unary minus at position " + i);
                    }
                }
                while (i < n && (Character.isDigit(tokens[i]) || tokens[i] == '.')) {
                    sb.append(tokens[i]);
                    i++;
                }
                i--; // backtrack one position because loop will increment

                try {
                    double val = Double.parseDouble(sb.toString());
                    values.push(val);
                    recordTrace(traces, sb.toString(), "Pushed operand " + val, values, ops);
                } catch (NumberFormatException e) {
                    return new EvaluationResult("Invalid number format: " + sb);
                }
            } else if (c == '(') {
                ops.push(c);
                recordTrace(traces, "(", "Pushed '(' onto operator stack", values, ops);
            } else if (c == ')') {
                while (!ops.isEmpty() && ops.peek() != '(') {
                    try {
                        applyTopOp(values, ops, traces);
                    } catch (ArithmeticException | IllegalStateException e) {
                        return new EvaluationResult(e.getMessage());
                    }
                }
                if (!ops.isEmpty() && ops.peek() == '(') {
                    ops.pop(); // pop '('
                    recordTrace(traces, ")", "Popped matched '('", values, ops);
                } else {
                    return new EvaluationResult("Mismatched parentheses detected.");
                }
            } else if (isOperator(c)) {
                while (!ops.isEmpty() && hasPrecedence(c, ops.peek())) {
                    try {
                        applyTopOp(values, ops, traces);
                    } catch (ArithmeticException | IllegalStateException e) {
                        return new EvaluationResult(e.getMessage());
                    }
                }
                ops.push(c);
                recordTrace(traces, String.valueOf(c), "Pushed operator '" + c + "' onto stack", values, ops);
            } else {
                return new EvaluationResult("Unexpected character '" + c + "' at position " + i);
            }
        }

        // Apply remaining operators
        while (!ops.isEmpty()) {
            if (ops.peek() == '(') {
                return new EvaluationResult("Mismatched parentheses remaining on stack.");
            }
            try {
                applyTopOp(values, ops, traces);
            } catch (ArithmeticException | IllegalStateException e) {
                return new EvaluationResult(e.getMessage());
            }
        }

        if (values.size() != 1) {
            return new EvaluationResult("Invalid expression: multiple values left without operators.");
        }

        double finalResult = values.pop();
        recordTrace(traces, "END", "Evaluation complete: result = " + finalResult, values, ops);
        return new EvaluationResult(finalResult, traces);
    }

    private static boolean isOperator(char op) {
        return op == '+' || op == '-' || op == '*' || op == '/';
    }

    private static boolean hasPrecedence(char op1, char op2) {
        if (op2 == '(' || op2 == ')') return false;
        if ((op1 == '*' || op1 == '/') && (op2 == '+' || op2 == '-')) return false;
        return true;
    }

    private static void applyTopOp(CustomStack<Double> values, CustomStack<Character> ops, List<StepTrace> traces) {
        if (values.size() < 2) {
            throw new IllegalStateException("Insufficient operands for operator '" + ops.peek() + "'.");
        }
        double b = values.pop();
        double a = values.pop();
        char op = ops.pop();

        double res;
        switch (op) {
            case '+': res = a + b; break;
            case '-': res = a - b; break;
            case '*': res = a * b; break;
            case '/':
                if (Math.abs(b) < 1e-9) {
                    throw new ArithmeticException("Division by zero error: cannot divide " + a + " by 0.");
                }
                res = a / b;
                break;
            default:
                throw new IllegalStateException("Unknown operator: " + op);
        }

        values.push(res);
        recordTrace(traces, String.valueOf(op), "Evaluated " + a + " " + op + " " + b + " = " + res, values, ops);
    }

    private static void recordTrace(List<StepTrace> traces, String token, String action,
                                    CustomStack<Double> values, CustomStack<Character> ops) {
        List<Double> valList = values.toList();
        List<String> opList = new ArrayList<>();
        for (Character c : ops.toList()) {
            opList.add(String.valueOf(c));
        }
        traces.add(new StepTrace(token, action, valList, opList));
    }
}
