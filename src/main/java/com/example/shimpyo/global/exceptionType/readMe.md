* 사용 방법
* import com.example.shimpyo.global.BaseException;
* import static com.example.shimpyo.global.exceptionType.MemberExceptionType.*;


* User user = userRepository.findById(~~~).orElseThrow(() -> new Exception(MEMBER_NOT_FOUND);