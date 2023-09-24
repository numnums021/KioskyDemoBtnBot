package kiosky.ru.KioskyDemoBtnBot.service;

import com.vdurmont.emoji.EmojiParser;
import kiosky.ru.KioskyDemoBtnBot.config.BotConfig;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

/*
 * @author: Fomin D.A.
 */
@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig config;
    private final EmojiParser emojiParser;

    private final String YES_BTN = "YES_BTN";
    private final String NO_BTN = "NO_BTN";

    private final String MY_DATA_TEXT = EmojiParser.parseToUnicode("У нас нет Ваших данных! \uD83D\uDE3C");
    private final String DELETE_DATA_TEXT = EmojiParser.parseToUnicode("У нас нет Ваших данных! \uD83D\uDE40");
    private final String HELP_TEXT = EmojiParser.parseToUnicode("Если есть какие-то вопросы или предложения можете написать в поддержку @kiosky_support.\n\n" +
            "Описание команд:\n/start - Получаем стартовое окно, которое появилось при первом сообщении боту!\n"+
            "/mydata - Получение своих данных, если они хранятся у нас в базе по прошлым заказам.\n"+
            "/deletedate - Удаление своих данных из нашей базы.\n"+
            "/help - Получение помощи по командам данного бота.\n\n"+
            " Так же в данном боте имеются кнопки, которые помогают освоиться. \uD83D\uDC40");
    private final String CURRENT_ORDER_TEXT = EmojiParser.parseToUnicode("Текущий заказ ещё на ферме пасётся. Приходите через полгода! \uD83E\uDD7A");
    private final String HISTORY_ORDER_TEXT = EmojiParser.parseToUnicode("Какая история? Бегом на завод, работяга! \uD83D\uDE3E");
    private final String REVIEW_TEXT = EmojiParser.parseToUnicode("Засунь его себе в Ass. \uD83D\uDE44");
    private final String NO_SWEARING_TEXT = EmojiParser.parseToUnicode("Пожалуйста, без мата! \uD83E\uDD21");
    private final String GOOD_NUMBER_TEXT = EmojiParser.parseToUnicode("Был отправлен номер телефона! Сейчас проверим заказ! \uD83D\uDE0D");
    private final String ERROR_NUMBER_TEXT = EmojiParser.parseToUnicode("О оу, кажется, ты ввёл что-то неизвестное для меня. \uD83D\uDC7F");


    public TelegramBot(BotConfig config){
       this.config = config;
       List<BotCommand> listOfCommands = new ArrayList<>();
       listOfCommands.add(new BotCommand("/start", "Получить приветствие опять"));
       listOfCommands.add(new BotCommand("/register", "Кое-что интересное.."));
       listOfCommands.add(new BotCommand("/mydata", "Информация о клиенте"));
       listOfCommands.add(new BotCommand("/deletedata", "Удалить свои данные"));
       listOfCommands.add(new BotCommand("/help", "Помощь"));
       try{
           this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
       }
       catch(TelegramApiException ex) {
           log.info("Ошибка при установке команд в боте! " + ex.getMessage());
       }
       this.emojiParser = new EmojiParser();
   }
    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.info("START onUpdateReceived");
        // Что должен делать бот, когда ему что-то пишут?
        if(update.hasMessage() && update.getMessage().hasText()){
            String messageText = update.getMessage().getText();
            // Бот должен знать кому должен писать!
            long chatId = update.getMessage().getChatId();
            log.info("\nПришло сообщение = " + update.getMessage().getText()+
                    "\nЧат = " + update.getMessage().getChat());
            switch (messageText){
                case "/start":
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/register":
                    register(chatId);
                    break;
                case "/mydata":
                    sendMessage(chatId, MY_DATA_TEXT);
                    break;
                case "/deletedata":
                    sendMessage(chatId, DELETE_DATA_TEXT);
                    break;
                case "/help":
                    sendMessage(chatId, HELP_TEXT);
                    break;

                case "Информация о текущем заказе":
                    sendMessage(chatId, CURRENT_ORDER_TEXT);
                    break;
                case "История заказов":
                    sendMessage(chatId, HISTORY_ORDER_TEXT);
                    break;
                case "Отзыв":
                    sendMessage(chatId, REVIEW_TEXT);
                    break;
                case "Пиздец":
                case "блять":
                case "нахуй":
                    sendMessage(chatId, NO_SWEARING_TEXT);
                    break;
                default:
                    // Проверяем на номер телефона
                    if (messageText.matches("^(7|8)?[-]?\\(?\\d{3}\\)?[-]?\\d{3}[-]?\\d{2}[-]?\\d{2}$")){
                        sendMessage(chatId, GOOD_NUMBER_TEXT);
                        break;
                    }
                    sendMessage(chatId, ERROR_NUMBER_TEXT);
                    break;

            }
            // Если нажали на инлайн кнопку под сообщением
        } else if (update.hasCallbackQuery()) {
            String callBackData = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (callBackData.equals(YES_BTN))
                executeEditMessageText(chatId, "inst: @sexy.boy", messageId);
            else if (callBackData.equals(NO_BTN))
                executeEditMessageText(chatId, ":c", messageId);
        }
    }

    private void executeEditMessageText(long chatId, String text, long messageId){
        EditMessageText messageText = new EditMessageText();
        messageText.setChatId(chatId);
        messageText.setText(text);
        messageText.setMessageId((int)messageId);
        try{
            execute(messageText);
        }
        catch (TelegramApiException ignored){
            log.info("Ошибка: " + ignored.getMessage());
        }
    }

    // Формирование первого сообщения клиенту
    private void startCommandReceived(long chatId, String FirstName){
       String answer = EmojiParser.parseToUnicode(
               "Йоу, " + FirstName + ", ты попал на первое сообщение бота " + getBotUsername() + " \uD83D\uDD25" +
                       "\n\nВы можете ввести свой номер телефона для того, чтобы узнать состояние заказа! \uD83C\uDF4C");
       sendMessage(chatId, answer);
    }

    // Формирование команды /register
    private void register(long chatId){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Вы действительно хотите заняться шрексом?");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        InlineKeyboardButton yesBtn = new InlineKeyboardButton();
        yesBtn.setText("Yep");
        yesBtn.setCallbackData("YES_BTN"); // Позволяет понять боту какая кнопка была нажата

        InlineKeyboardButton noBtn = new InlineKeyboardButton();
        noBtn.setText("Nope");
        noBtn.setCallbackData("NO_BTN"); // Лучше использовать, как CONST

        rowInline.add(yesBtn); rowInline.add(noBtn);
        rowsLine.add(rowInline);

        inlineKeyboardMarkup.setKeyboard(rowsLine);
        message.setReplyMarkup(inlineKeyboardMarkup);

        try{
            execute(message);
        }
        catch (TelegramApiException ignored){
            log.info("Ошибка: " + ignored.getMessage());
        }

    }
    // Отправка стандартных сообщений клиенту
    private void sendMessage(long chatId, String textToSend){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        // Создаём клавиатуру на каждое сообщение
        createKeyboard(message);

        try{
            execute(message);
        }
        catch (TelegramApiException ignored){
            log.info("Ошибка: " + ignored.getMessage());
        }
    }

    // Создание клавиатуры МЕНЮ в чате бота
    private void createKeyboard(SendMessage message){
        // Создаём класс кнопок МЕНЮ для отображения клиенту!
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

        // Создаём и заполняем список с меню по рядам
        List<KeyboardRow> keyboardRowList = new ArrayList<>();

        // 1 Ряд
        KeyboardRow row = new KeyboardRow();
        row.add("Информация о текущем заказе");
        row.add("История заказов");
        row.add("Отзыв");
        keyboardRowList.add(row);

        // 2 Ряд
        row = new KeyboardRow();
        row.add("Пиздец");
        row.add("нахуй");
        row.add("блять");
        keyboardRowList.add(row);

        // Устанавливаем МЕНЮ список кнопок по рядам
        keyboardMarkup.setKeyboard(keyboardRowList);

        // Привязываем МЕНЮ к сообщению
        message.setReplyMarkup(keyboardMarkup);
    }
}
