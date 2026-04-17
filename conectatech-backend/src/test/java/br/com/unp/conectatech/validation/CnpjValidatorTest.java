package br.com.unp.conectatech.validation;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class CnpjValidatorTest {

    @Test
    void cnpj_valido_formatado_deve_ser_aceito() {
        assertThat(CnpjValidator.isValid("11.222.333/0001-81")).isTrue();
    }

    @Test
    void cnpj_valido_sem_mascara_deve_ser_aceito() {
        assertThat(CnpjValidator.isValid("11222333000181")).isTrue();
    }

    @Test
    void cnpj_com_digitos_invalidos_deve_ser_rejeitado() {
        assertThat(CnpjValidator.isValid("11.222.333/0001-00")).isFalse();
    }

    @Test
    void cnpj_com_todos_digitos_iguais_deve_ser_rejeitado() {
        assertThat(CnpjValidator.isValid("00000000000000")).isFalse();
        assertThat(CnpjValidator.isValid("11111111111111")).isFalse();
        assertThat(CnpjValidator.isValid("99999999999999")).isFalse();
    }

    @Test
    void cnpj_com_tamanho_errado_deve_ser_rejeitado() {
        assertThat(CnpjValidator.isValid("1122233300018")).isFalse(); // 13 dígitos
        assertThat(CnpjValidator.isValid("112223330001810")).isFalse(); // 15 dígitos
    }

    @Test
    void cnpj_nulo_deve_ser_rejeitado() {
        assertThat(CnpjValidator.isValid(null)).isFalse();
    }

    @Test
    void cnpj_vazio_deve_ser_rejeitado() {
        assertThat(CnpjValidator.isValid("")).isFalse();
        assertThat(CnpjValidator.isValid("   ")).isFalse();
    }

    @Test
    void cnpj_com_letras_deve_ser_rejeitado() {
        assertThat(CnpjValidator.isValid("AB.CDE.FGH/IJKL-MN")).isFalse();
    }
}
