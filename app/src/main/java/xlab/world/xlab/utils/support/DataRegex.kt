package xlab.world.xlab.utils.support

import android.util.Patterns

object DataRegex {
    private val regexNum = Regex("""\d+""") // for number format check
    private val regexEn = Regex("[a-zA-Z]") // for alphabet format check
    private val regexMail = Patterns.EMAIL_ADDRESS // for mail format check

    private const val passwordMinLength = 6
    private const val passwordMaxLength = 20

    private const val nickNameMinLength = 3
    private const val nickNameMaxLength = 10

    // 이메일 정규식
    fun emailRegex(email: String): Boolean {
        return regexMail.matcher(email).matches()
    }

    // 비밀번호 길이 정규식
    fun passwordLengthRegex(password: String): Boolean {
        return (password.length in passwordMinLength..passwordMaxLength)
    }

    // 비밀번호 정규식 (영문자 + 숫자)
    fun passwordTextRegex(password: String): Boolean {
        if (!password.contains(" ") && regexNum.containsMatchIn(password)) { // 공란, 숫자 포함 체크
            val nonNum = password.replace(regexNum, "") // 비밀번호에 숫자 제거
            if (nonNum != "" && regexEn.containsMatchIn(nonNum)) { // 영문자 포함 포함 체크
                return true
            }
        }
        return false
    }

    // 닉네임 정규식
    fun nickNameRegex(nickName: String): Boolean {
        return (nickName.length in nickNameMinLength..nickNameMaxLength)
    }
}