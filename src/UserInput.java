import java.util.Map;
import java.util.Scanner;

public class UserInput {
    String input;
    String condCode = "1110";
    String opType = "000";
    String S = "0";
    Map<String, String> mapShiftType = Map.of(
            "LSL", "00",
            "LSR", "01",
            "ASR", "10",
            "MOV", "00",
            "ROR", "11",
            "RRX", "11"
    );
    Map<String, String> mapOpCode = Map.of(
            "ADD", "0100",
            "SUB", "0010",
            "MUL", "000",
            "MOV", "1101",
            "LSL", "1101",
            "ASR", "1101",
            "LDR", "1100",
            "STR", "1100"
    );
    Map<Integer, String> mapHex = Map.of(
            10, "A",
            11, "B",
            12, "C",
            13, "D",
            14, "E",
            15, "F"
    );

    // Retrieve ARM instruction from user
    void getInstructionChoice() {
        Scanner input = new Scanner(System.in);
        System.out.println("Enter the ARM instruction (eg ADD r0, r1, r2)");
        this.input = input.nextLine().toUpperCase();
        input.close();
    }

    // Convert binary to hex
    String binaryToHex(String binary) {
        if (binary.length() % 4 == 0) {
            int binaryNow = 0;
            StringBuilder result = new StringBuilder();
            int j = 0;
            for (int i = binary.length() - 1; i >= -1; i--) {
                if (j % 4 == 0 && j > 0) {
                    if (binaryNow > 9) {
                        result.insert(0, mapHex.get(binaryNow));
                    } else {
                        result.insert(0, binaryNow);
                    }
                    binaryNow = 0;
                }
                if (i < 0) {
                    break;
                }
                binaryNow += Math.pow(2.0, j % 4) * (binary.charAt(i) - '0');
                j++;
            }
            return result.toString();
        }
        return binary;

    }

    // Convert ARM instructions to machine code
    String convertInstruction() {
        input = input.replace("[", "");
        input = input.replace("]", "");
        String[] splitInput = input.split("[\\s,]+");
        String operationType = splitInput[0];
        String rd = intToBinary(Integer.parseInt(splitInput[1].substring(1)), 4);
        String opCode = mapOpCode.get(operationType);
        String rn = "0000";
        String multcd = "1001";

        if (operationType.equals("MOV") && splitInput[2].charAt(0) == '#') {
            opType = "001";
            String immediate = intToBinary(Integer.parseInt(splitInput[2].substring(1)), 12);
            return condCode + opType + opCode + S + rn + rd + immediate;
        }

        // If the ARM instruction length after being split has length 3,
        // it has to be a MOV or the equivalent to an LDR/SDR with a #0 offset
        if (splitInput.length == 3) {
            String shiftAmt = "00000";
            String shiftType = "00";
            String SR = "0";
            String rm = intToBinary(Integer.parseInt(splitInput[2].substring(1)), 4);
            if (checkForLDR_STR(operationType)) {
                input = operationType + " " + splitInput[1] + ", " + splitInput[2] + ", #0";
                splitInput = input.split("[\\s,]+");
            } else {
                return condCode + opType + opCode + S + rn + rd + shiftAmt + shiftType + SR + rm;
            }
        }

        // Multiply
        if (operationType.equals("MUL") || operationType.equals("MLA")) {
            String rm = intToBinary(Integer.parseInt(splitInput[2].substring(1)), 4);
            String rs = intToBinary(Integer.parseInt(splitInput[3].substring(1)), 4);
            String A = "0";
            if (operationType.equals("MLA")) {
                A = "1";
                rn = intToBinary(Integer.parseInt(splitInput[4].substring(1)), 4);
            }
            return condCode + opType + opCode + A + S + rd + rn + rs + multcd + rm;
        }

        // If the instruction length after splitting is 4, it can be anything
        // but a shift (other  than an instruction that starts with a shift)
        if (splitInput.length == 4) {

            // Assigns variables if its a LDR or STR
            boolean LDRorSTR = checkForLDR_STR(operationType);

            // Check if it is an Immediate
            if (splitInput[3].charAt(0) == '#') {
                String immediate = splitInput[3].substring(1);

                // More assignments if it is LDR or STR
                if (LDRorSTR) {
                    opType = "010";
                    rn = intToBinary(Integer.parseInt(splitInput[2].substring(1)), 4);

                    // Handles if it is a negative immediate
                    if (immediate.charAt(0) == '-') {
                        opCode = "1000";
                        immediate = immediate.substring(1);
                    }
                    immediate = intToBinary(Integer.parseInt(immediate), 12);
                    return condCode + opType + opCode + S + rn + rd + immediate;

                    // for ADD and SUB with immediate
                } else if (operationType.equals("ADD") || operationType.equals("SUB")) {
                    rn = intToBinary(Integer.parseInt(splitInput[2].substring(1)), 4);
                    opType = "001";
                    immediate = intToBinary(Integer.parseInt(immediate), 12);
                    return condCode + opType + opCode + S + rn + rd + immediate;
                } else {
                    // for all other operation types with immediate
                    String shiftAmt = intToBinary(Integer.parseInt(splitInput[3].substring(1)), 5);
                    String shiftType = mapShiftType.get(operationType);
                    String SR = "0";
                    String rm = intToBinary(Integer.parseInt(splitInput[2].substring(1)), 4);
                    return condCode + opType + opCode + S + rn + rd + shiftAmt + shiftType + SR + rm;
                }
            } else {
                // LDR or STR with register
                if (LDRorSTR) {
                    rn = intToBinary(Integer.parseInt(splitInput[2].substring(1)), 4);
                    String rm = intToBinary(Integer.parseInt(splitInput[3].substring(1)), 4);
                    return condCode + opType + opCode + S + rn + rd + "00000000" + rm;

                    // ADD or SUB with register
                } else if (operationType.equals("ADD") || operationType.equals("SUB")) {
                    rn = intToBinary(Integer.parseInt(splitInput[2].substring(1)), 4);
                    String rm = intToBinary(Integer.parseInt(splitInput[3].substring(1)), 4);
                    return condCode + opType + opCode + S + rn + rd + "00000000" + rm;

                    // All else with register
                } else {
                    String rs = intToBinary(Integer.parseInt(splitInput[3].substring(1)), 4);
                    String shiftType = mapShiftType.get(operationType);
                    String SR = "1";
                    String rm = intToBinary(Integer.parseInt(splitInput[2].substring(1)), 4);
                    return condCode + opType + opCode + S + rn + rd + rs + 0 + shiftType + SR + rm;
                }
            }
        }

        // When input split has length 5, it must be a MOV with shift
        if (splitInput.length == 5) {
            if (!operationType.equals("MOV")) {
                return "Bad input.  Did you mean MOV?";
            }
            String shiftType = mapShiftType.get(splitInput[3]);
            String rm = intToBinary(Integer.parseInt(splitInput[2].substring(1)), 4);

            // shift with an immediate
            if (splitInput[4].charAt(0) == '#') {
                String SR = "0";
                String shiftAmt = intToBinary(Integer.parseInt(splitInput[4].substring(1)), 5);
                return condCode + opType + opCode + S + rn + rd + shiftAmt + shiftType + SR + rm;
            }

            // shift with a register
            String rs = intToBinary(Integer.parseInt(splitInput[4].substring(1)), 4);
            String SR = "1";
            return condCode + opType + opCode + S + rn + rd + rs + 0 + shiftType + SR + rm;
        }

        // when input split is length 6, it must be a shift
        if (splitInput.length == 6) {
            String shiftType = mapShiftType.get(splitInput[4]);
            String rm = intToBinary(Integer.parseInt(splitInput[3].substring(1)), 4);
            checkForLDR_STR(operationType);
            rn = intToBinary(Integer.parseInt(splitInput[2].substring(1)), 4);

            // shift with immediate
            if (splitInput[5].charAt(0) == '#') {
                String shiftAmt = intToBinary(Integer.parseInt(splitInput[5].substring(1)), 5);
                String SR = "0";
                return condCode + opType + opCode + S + rn + rd + shiftAmt + shiftType + SR + rm;
            }

            // shift with register
            String rs = intToBinary(Integer.parseInt(splitInput[5].substring(1)), 4);
            String SR = "1";
            return condCode + opType + opCode + S + rn + rd + rs + 0 + shiftType + SR + rm;

        }

        // if we reach here something has gone wrong
        return "Unable to process ARM to Machine Code conversion";
    }

    // consistent assignments when LDR or STR
    boolean checkForLDR_STR(String operationType) {
        if (operationType.equals("LDR") || operationType.equals("STR")) {
            opType = "011";
            if (operationType.equals("LDR")) {
                S = "1";
            }
            return true;
        }
        return false;
    }

    // convert an integer to 8 bit binary with 4 bit rotation
    String intToBinary(int num, int bits) {
        StringBuilder result = new StringBuilder();
        while (num > 0) {
            result.insert(0, num % 2);
            num /= 2;
        }
        String stringResult = result.toString();
        if (stringResult.length() > 8) {
            int remove0s = stringResult.length() - 8 + (stringResult.length() - 8) % 2;
            if (stringResult.substring(stringResult.length() - 4).contains("1")) {
                // error handle
            }
            stringResult = stringResult.substring(0, stringResult.length() - remove0s);
            String rotation = intToBinary((32 - remove0s) / 2, 4);
            return rotation + stringResult;
        }
        while (result.length() < bits) {
            result.insert(0, 0);
        }
        return result.toString();
    }

}
