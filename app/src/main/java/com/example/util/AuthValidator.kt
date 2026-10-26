package com.example.util

import java.util.Locale

/**
 * Resultado compreensível de uma validação de campo.
 */
sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()

    val isValid: Boolean get() = this is Success
    val errorMessage: String? get() = (this as? Error)?.message
}

/**
 * Utilitário centralizado para sanitização e validação de credenciais de autenticação.
 */
object AuthValidator {

    private val EMAIL_REGEX = Regex(
        "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,63}$", 
        RegexOption.IGNORE_CASE
    )

    /**
     * Sanitiza o e-mail: remove espaços em branco (trim), remove espaços internos 
     * e converte para minúsculas. Lida com nulos retornando string vazia.
     */
    fun sanitizeEmail(email: String?): String {
        if (email == null) return ""
        return email.trim()
            .replace("\\s+".toRegex(), "")
            .lowercase(Locale.ROOT)
    }

    /**
     * Sanitiza a senha: remove espaços em branco nas extremidades.
     */
    fun sanitizePassword(password: String?): String {
        return password?.trim() ?: ""
    }

    /**
     * Valida o formato do e-mail após sanitização.
     */
    fun validateEmail(email: String?): ValidationResult {
        val sanitized = sanitizeEmail(email)
        return when {
            sanitized.isBlank() -> ValidationResult.Error("O e-mail não pode estar vazio.")
            sanitized.length > 254 -> ValidationResult.Error("O e-mail é muito longo.")
            !EMAIL_REGEX.matches(sanitized) -> ValidationResult.Error("Por favor, insira um e-mail válido.")
            else -> ValidationResult.Success
        }
    }

    /**
     * Valida a senha e sua força básica.
     */
    fun validatePassword(password: String?, minLength: Int = 8): ValidationResult {
        val sanitized = sanitizePassword(password)
        return when {
            sanitized.isBlank() -> ValidationResult.Error("A senha não pode estar vazia.")
            sanitized.length < minLength -> ValidationResult.Error("A senha deve ter pelo menos $minLength caracteres.")
            sanitized.any { it.isWhitespace() } -> ValidationResult.Error("A senha não pode conter espaços.")
            else -> ValidationResult.Success
        }
    }

    /**
     * Valida o nome do usuário.
     */
    fun validateName(name: String?): ValidationResult {
        val sanitized = name?.trim() ?: ""
        return when {
            sanitized.isBlank() -> ValidationResult.Error("O nome não pode estar vazio.")
            sanitized.length < 2 -> ValidationResult.Error("O nome é muito curto.")
            sanitized.length > 100 -> ValidationResult.Error("O nome é muito longo.")
            else -> ValidationResult.Success
        }
    }

    /**
     * Avalia a força da senha para feedback visual (0 a 5).
     */
    fun calculatePasswordStrength(password: String): Int {
        if (password.isEmpty()) return 0
        var score = 0
        if (password.length >= 8) score++
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isLowerCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++
        return score
    }
}
