package xlab.world.xlab.utils.support

import android.util.Patterns
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DataRegex {
    private val regexNum = Regex("""\d+""") // for number format check
    private val regexEn = Regex("[a-zA-Z]") // for alphabet format check
    private val regexMail = Patterns.EMAIL_ADDRESS // for mail format check

    private const val passwordMinLength = 6
    private const val passwordMaxLength = 20

    private const val nickNameMinLength = 3
    private const val nickNameMaxLength = 10

    private const val birthYearMin = 1950

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

    // 생년월일 정규식
    fun birthRegex(birth: Int): Boolean {
        return (birth in birthYearMin..Calendar.getInstance().get(Calendar.YEAR))
    }
    // 생년월일 정규식 (년,월,일 비교)
    fun birthRegex(birthday: String): Boolean {
        if (birthday.length < 10) return false
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateFormat.isLenient = false
            val date: Date = dateFormat.parse(birthday)
            val nowDate = Date(System.currentTimeMillis())

            val dateCalendar = Calendar.getInstance()
            dateCalendar.time = date

            val nowDateCalendar = Calendar.getInstance()
            nowDateCalendar.time = nowDate

            return !(dateCalendar.get(Calendar.YEAR) < birthYearMin || dateCalendar.after(nowDateCalendar))

        } catch (e: ParseException) {
            return false
        }
    }
}