public record Question(int id, String prompt, String optionA, String optionB,
                       String optionC, String optionD, String correctOption) {
    public String optionFor(String letter) {
        return switch (letter) {
            case "A" -> optionA;
            case "B" -> optionB;
            case "C" -> optionC;
            default -> optionD;
        };
    }
}
