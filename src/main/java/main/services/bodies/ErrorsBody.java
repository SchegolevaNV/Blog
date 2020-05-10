package main.services.bodies;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorsBody
{
    //"title": "Заголовок не установлен",
    //"title": "Заголовок слишком короткий",
    //"text": "Текст публикации слишком короткий"
    //"text": "Текст комментария не задан или слишком короткий"
//    "code": "Ссылка для восстановления пароля устарела.
//<a href=
//        \"/auth/restore\">Запросить ссылку снова</a>",
//        "password": "Пароль короче 6-ти символов",
//        "captcha": "Код с картинки введён неверно"
//    "email": "Этот e-mail уже зарегистрирован",
//            "name": "Имя указано неверно",
    //"photo": "Фото слишком большое, нужно не более 5 Мб",

    private String title;
    private String text;
    private String code;
    private String password;
    private String captcha;
    private String email;
    private String name;
    private String photo;
}
