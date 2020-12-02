package main.model.enums;

public enum Errors
{
    TITLE_IS_NOT_SET_OR_SHORT("Заголовок не установлен или слишком короткий, введите минимум 3 символа"),
    TEXT_IS_NOT_SET_OT_SHORT("Текст публикации не установлен или слишком короткий, " +
            "публикация должна быть от 50-ти символов"),
    IMAGE_IS_BIG("Размер файла превышает допустимый размер, нужно не более 5 Мб"),
    COMMENT_IS_EMPTY_OR_SHORT("Текст комментария не задан или слишком короткий"),
    COMMENT_FOR_ANSWER_IS_NOT_EXIST("Комментарий, на который вы пытаетесь ответить, не существует"),
    POST_FOR_COMMENT_IS_NOT_EXIST("Пост, к которому вы пишите комментарий, не существует"),
    CODE_IS_OUT_OF_DATE("Ссылка для восстановления пароля устарела. " +
            "<a href=\"/login/restore-password\">Запросить ссылку снова</a>"),
    PASSWORD_IS_SHORT("Пароль короче 6-ти символов"),
    CAPTCHA_IS_INCORRECT("Код с картинки введён неверно"),
    THIS_EMAIL_IS_EXIST("Этот e-mail уже зарегистрирован"),
    NAME_IS_INCORRECT("Имя указано неверно"),
    EMAIL_IS_INCORRECT("E-mail указан неверно"),
    USER_IS_NOT_MODERATOR("Вы не являетесь модератором"),
    IMAGE_INVALID_FORMAT("Изображение должно быть формата JPG/PNG"),
    IMAGE_INVALID_NAME("Название файла должно содержать только латинские буквы, цифры и знаки подчёркивания");

    private String title;

    Errors(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
