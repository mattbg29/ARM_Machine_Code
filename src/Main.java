/* *****************************************************************************
 * Name: Matthew Green
 * Date: 29Nov2022
 * Purpose:  To convert ARM instructions into machine code
 * Note: this does not currently have great error handling, but does
 * handle the conversion correctly when a valid input is given
 **************************************************************************** */

public class Main {
    public static void main(String[] args) {
        UserInput obj = new UserInput();
        obj.getInstructionChoice();
        String output = obj.convertInstruction();
        System.out.println("Machine code (hex): " + obj.binaryToHex(output));
    }
}
