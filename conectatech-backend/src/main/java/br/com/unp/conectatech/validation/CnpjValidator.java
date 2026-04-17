package br.com.unp.conectatech.validation;

/**
 * Validador de CNPJ — verifica formato e dígitos verificadores.
 * Remove máscara antes de validar.
 */
public class CnpjValidator {

    private CnpjValidator() {
    }

    public static boolean isValid(String cnpj) {
        if (cnpj == null || cnpj.isBlank())
            return false;

        // Remove máscara
        String digits = cnpj.replaceAll("[.\\-/]", "").trim();

        if (digits.length() != 14)
            return false;
        if (!digits.matches("\\d{14}"))
            return false;

        // Rejeita sequências repetidas
        if (digits.chars().distinct().count() == 1)
            return false;

        // Calcula primeiro dígito verificador
        int[] weights1 = { 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2 };
        int sum1 = 0;
        for (int i = 0; i < 12; i++) {
            sum1 += Character.getNumericValue(digits.charAt(i)) * weights1[i];
        }
        int remainder1 = sum1 % 11;
        int d1 = (remainder1 < 2) ? 0 : (11 - remainder1);

        if (Character.getNumericValue(digits.charAt(12)) != d1)
            return false;

        // Calcula segundo dígito verificador
        int[] weights2 = { 6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2 };
        int sum2 = 0;
        for (int i = 0; i < 13; i++) {
            sum2 += Character.getNumericValue(digits.charAt(i)) * weights2[i];
        }
        int remainder2 = sum2 % 11;
        int d2 = (remainder2 < 2) ? 0 : (11 - remainder2);

        return Character.getNumericValue(digits.charAt(13)) == d2;
    }
}
