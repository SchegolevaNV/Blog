package main.model.enums;

public enum Errors
{
    TitleIsNotSet ("Заголовок не установлен"),
    TitleIsShort ("Заголовок слишком короткий"),
    TextIsShort ("Текст публикации слишком короткий"),
    CommentIsEmptyOrShort ("Текст комментария не задан или слишком короткий"),
    CodeIsOutOfDate ("Ссылка для восстановления пароля устарела. " +
            "<a href=\"/auth/restore\">Запросить ссылку снова</a>"),
    PasswordIsShort ("Пароль короче 6-ти символов"),
    CaptchaIsIncorrect ("Код с картинки введён неверно"),
    ThisEmailIsExist ("Этот e-mail уже зарегистрирован"),
    NameIsIncorrect ("Имя указано неверно"),
    PhotoIsBig ("Фото слишком большое, нужно не более 5 Мб");

    private String title;

    Errors(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
