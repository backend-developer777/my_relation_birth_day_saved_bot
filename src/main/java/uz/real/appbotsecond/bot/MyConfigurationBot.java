package uz.real.appbotsecond.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.real.appbotsecond.model.BirthDay;
import uz.real.appbotsecond.repository.BirthDayRepository;
import uz.real.appbotsecond.service.TelegramBotService;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;

@Component
public class MyConfigurationBot extends TelegramLongPollingBot {

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Autowired
    private BirthDayRepository birthDayRepository;

    @Autowired
    private TelegramBotService telegramBotService;


    private String fullName = "";
    private long birthId = 0;

    @Override
    public void onUpdateReceived(Update update) {

        String regex = "^[0-3]?[0-9]/[0-3]?[0-9]/(?:[0-9]{2})?[0-9]{2}$";
        Pattern pattern = Pattern.compile(regex);

        String regexFullName = "^[a-zA-Z0-9 ]+$";
        Pattern patternFullName = Pattern.compile(regexFullName);

        if (update.hasMessage()) {
            Message message = update.getMessage();
            long chatId = message.getChatId();
            if (message.hasText()) {
                String messageText = message.getText();
                if (messageText.equalsIgnoreCase("/start") || message.getText().equalsIgnoreCase("/")) {
                    SendMessage sendMessage = telegramBotService.useBot(chatId);
                    try {
                        execute(sendMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                } else if (patternFullName.matcher(messageText).matches()) {
                    fullName = messageText;
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(chatId);
                    sendMessage.setText("Tug'ilgan sanasini kiriting masalan: 28/11/1998 ko'rinishida!");
                    try {
                        execute(sendMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                } else if (pattern.matcher(messageText).matches()) {
                    SendMessage sendMessage = telegramBotService.checkDate(messageText, message, fullName, birthId);
                    try {
                        execute(sendMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();

            if (callbackQuery.getData().equalsIgnoreCase("Botdan foydalanish!")) {
                SendMessage sendMessage = telegramBotService.saveUser(callbackQuery, callbackQuery.getMessage().getChat().getId());
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (callbackQuery.getData().equalsIgnoreCase("Yana do'stlarni saqlash!")) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(callbackQuery.getMessage().getChat().getId());
                sendMessage.setText("Do'stingizning ismi va familyasini kiriting! masalan: Sherzod Nurmatov ko'rinishida");
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (callbackQuery.getData().equalsIgnoreCase("Muhim sanalarim!")) {
                SendMessage sendMessage = telegramBotService.myImportantMessage(callbackQuery.getMessage().getChat().getId(), callbackQuery);
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (callbackQuery.getData().equalsIgnoreCase("view")) {
                SendMessage sendMessage = telegramBotService.viewAll(callbackQuery, callbackQuery.getMessage().getChatId());
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }else if (callbackQuery.getData().equalsIgnoreCase("Orqaga!")) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(callbackQuery.getMessage().getChatId());
                sendMessage.setText("Enter to /start or /");
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
            else if (callbackQuery.getData().substring(0, 1).equalsIgnoreCase("p")) {
                SendMessage prev = telegramBotService.prev(callbackQuery.getData(), callbackQuery.getMessage().getChatId());
                try {
                    execute(prev);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (callbackQuery.getData().substring(0, 1).equalsIgnoreCase("n")) {
                SendMessage next = telegramBotService.next(callbackQuery.getData(), callbackQuery.getMessage().getChatId(), callbackQuery);
                try {
                    execute(next);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (callbackQuery.getData().equalsIgnoreCase("add")) {
                SendMessage add = telegramBotService.add(callbackQuery.getMessage().getChatId());
                try {
                    execute(add);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (callbackQuery.getData().substring(0, 1).equalsIgnoreCase("e")) {
                birthId = Long.parseLong(callbackQuery.getData().substring(1));
                SendMessage edit = telegramBotService.edit(callbackQuery.getData(), callbackQuery.getMessage().getChatId());
                try {
                    execute(edit);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (callbackQuery.getData().substring(0, 1).equalsIgnoreCase("d")) {
                SendMessage delete = telegramBotService.delete(callbackQuery.getData(), callbackQuery.getMessage().getChatId());
                try {
                    execute(delete);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (callbackQuery.getData().substring(0, 2).equalsIgnoreCase("Ha")) {
                SendMessage sendMessage = telegramBotService.successDelete(callbackQuery.getData(), callbackQuery.getMessage().getChatId());
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (callbackQuery.getData().substring(0, 2).equalsIgnoreCase("Yo")) {
                SendMessage sendMessage = telegramBotService.notDelete(callbackQuery.getData(), callbackQuery.getMessage().getChatId());
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    private void budilnik() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        inlineKeyboardButton.setText("Yana do'stlarni saqlash!");
        inlineKeyboardButton.setCallbackData("Yana do'stlarni saqlash!");
        inlineKeyboardButton1.setText("Muhim sanalarim!");
        inlineKeyboardButton1.setCallbackData("Muhim sanalarim!");
        List<InlineKeyboardButton> keyboardButtonList = new LinkedList<>();
        List<InlineKeyboardButton> inlineKeyboardButtons = new LinkedList<>();
        keyboardButtonList.add(inlineKeyboardButton);
        inlineKeyboardButtons.add(inlineKeyboardButton1);
        List<List<InlineKeyboardButton>> inlineRows = new LinkedList<>();
        inlineRows.add(keyboardButtonList);
        inlineRows.add(inlineKeyboardButtons);
        inlineKeyboardMarkup.setKeyboard(inlineRows);
        LocalDate localDate = LocalDate.now();
        SendMessage sendMessage = new SendMessage();
        List<BirthDay> selectBirthList = birthDayRepository.find(localDate.getMonthValue(), localDate.getDayOfMonth());
        if (selectBirthList.size() > 0) {
            sendMessage.setReplyMarkup(inlineKeyboardMarkup);
            for (BirthDay birthDay : selectBirthList) {
                sendMessage.setChatId(birthDay.getUser().getChatId());
                sendMessage.setText("Bugun " + birthDay.getFullName() + " ning tug'ilgan kuni! Tabriklab qo'yishni unutmang!");
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Xabar yuborildi!");
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}