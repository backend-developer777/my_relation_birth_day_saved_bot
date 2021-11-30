package uz.real.appbotsecond.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.real.appbotsecond.model.BirthDay;
import uz.real.appbotsecond.payload.ResBirthDay;
import uz.real.appbotsecond.repository.BirthDayRepository;
import uz.real.appbotsecond.repository.UserRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class MyConfigurationBot extends TelegramLongPollingBot {

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BirthDayRepository birthDayRepository;

    private String fullName = "";
    LocalDate budilnikDate;

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
                    SendMessage sendMessage = useBot(chatId);
                    try {
                        execute(sendMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                } else if (patternFullName.matcher(messageText).matches()) {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(chatId);
                    sendMessage.setText("Tug'ilgan sanasini kiriting masalan: 28/11/1998 ko'rinishida");
                    fullName = messageText;
                    try {
                        execute(sendMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                } else if (pattern.matcher(messageText).matches()) {
                    SendMessage sendMessage = checkDate(messageText, message);
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
                SendMessage sendMessage = saveUser(callbackQuery, callbackQuery.getMessage().getChat().getId());
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
            }
            else if (callbackQuery.getData().equalsIgnoreCase("Muhim sanalarim!")){
                SendMessage sendMessage = myImportMessage(callbackQuery.getMessage().getChat().getId(), callbackQuery);
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }

        }
    }
    private SendMessage useBot(long chatId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText("Botdan foydalanish!");
        inlineKeyboardButton.setCallbackData("Botdan foydalanish!");
        List<InlineKeyboardButton> keyboardButtonRowOne = new LinkedList<>();
        keyboardButtonRowOne.add(inlineKeyboardButton);

        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("Muhim sanalarim!");
        inlineKeyboardButton1.setCallbackData("Muhim sanalarim!");
        List<InlineKeyboardButton> keyboardButtonRowTwo = new LinkedList<>();
        keyboardButtonRowTwo.add(inlineKeyboardButton1);
        List<List<InlineKeyboardButton>> inlineRows = new LinkedList<>();
        inlineRows.add(keyboardButtonRowOne);
        inlineRows.add(keyboardButtonRowTwo);
        inlineKeyboardMarkup.setKeyboard(inlineRows);
        return new SendMessage()
                .setChatId(chatId)
                .setText("Assalomu alaykum, yaqinlaringizni tug'ilgan kunini saqlab boruvchi botga hush kelibsiz!")
                .setReplyMarkup(inlineKeyboardMarkup);
    }

    private SendMessage saveUser(CallbackQuery callbackQuery, long chatId) {
        User from = callbackQuery.getFrom();
        Optional<uz.real.appbotsecond.model.User> byUsername = userRepository.findByUsername(from.getUserName());
        if (byUsername.isPresent()) {
            return new SendMessage()
                    .setChatId(chatId)
                    .setText("Siz avval ham ushbu botdan foydalangansiz, marxamat do'stingizning ismi va familyasini kiriting!" +
                            "masalan Sherzod Nurmatov ko'rinishida");
        }
        uz.real.appbotsecond.model.User user = new uz.real.appbotsecond.model.User();
        user.setFirstName(from.getFirstName());
        user.setUsername(from.getUserName());
        user.setChatId(String.valueOf(chatId));
        userRepository.save(user);
        return new SendMessage()
                .setChatId(chatId)
                .setText("Do'stingizning ismi va familyasini kiriting masalan: Sherzod Nurmatov ko'rinishida!");
    }

    private SendMessage checkDate(String stringDate, Message message) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText("Yana do'stlarni saqlash!");
        inlineKeyboardButton.setCallbackData("Yana do'stlarni saqlash!");
        List<InlineKeyboardButton> keyboardButtonList = new LinkedList<>();
        keyboardButtonList.add(inlineKeyboardButton);
        List<List<InlineKeyboardButton>> inlineRows = new LinkedList<>();
        inlineRows.add(keyboardButtonList);
        inlineKeyboardMarkup.setKeyboard(inlineRows);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        int day = Integer.parseInt(stringDate.substring(0, 2));
        int month = Integer.parseInt(stringDate.substring(3, 5));
        int year = Integer.parseInt(stringDate.substring(6, stringDate.length()-1));
        LocalDate nowDate = LocalDate.now();
        if (day >= 1 && day <= 31) {
            if (month >= 1 && month <= 12) {
                if (year <= nowDate.getYear()) {
                    budilnikDate = LocalDate.parse(stringDate, formatter);
                    BirthDay savedBirthDay = new BirthDay();
                    savedBirthDay.setBirthDayDate(budilnikDate);
                    savedBirthDay.setFullName(fullName);
                    savedBirthDay.setUser(userRepository.getByUsername(message.getFrom().getUserName()));
                    birthDayRepository.save(savedBirthDay);
                    return new SendMessage()
                            .setChatId(message.getChatId())
                            .setText("Tabriklaymiz siz do'stingizni tug'ilgan kunini muvafaqqiyatli saqladingiz, botning o'zi sizga xabar yuboradi!")
                            .setReplyMarkup(inlineKeyboardMarkup);
                } else {
                    return new SendMessage()
                            .setChatId(message.getChatId())
                            .setText("Yilni to'g'ri kiriting, yil 2022 dan kichik bo'lishi kerak. Siz " + year + " kiritdingiz!");
                }
            } else {
                return new SendMessage()
                        .setChatId(message.getChatId())
                        .setText("Oyni to'g'ri kiriting, oy (1-12) oralig'ida bo'lishi kerak! Siz " + month + " kiritdingiz!");
            }
        } else {
            return new SendMessage()
                    .setChatId(message.getChatId())
                    .setText("Kunni to'g'ri kiriting, kun (1-31) oralig'ida bo'lishi kerak! Siz " + day + " kiritdingiz!");
        }
    }

    @Scheduled(cron = "0 59 16 * * *")
    private void budilnik() {
        LocalDate localDate = LocalDate.now();
        SendMessage sendMessage = new SendMessage();
//        List<BirthDay> all = birthDayRepository.findAll();
//        if (all.size()>0) {
//            for (BirthDay birthDay : all) {
//                if (birthDay.getBirthDayDate().getMonthValue() == localDate.getMonthValue() && birthDay.getBirthDayDate().getDayOfMonth() == localDate.getDayOfMonth()) {
//                    sendMessage.setChatId(birthDay.getUser().getChatId());
//                    sendMessage.setText("Bugun " + birthDay.getFullName() + " do'stingizning tug'ilgan kuni! tabriklab qo'yishni unutmang!");
//                }try {
//                    execute(sendMessage);
//                } catch (TelegramApiException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
        List<BirthDay> selectBirthList = birthDayRepository.find(localDate.getDayOfMonth(), localDate.getMonthValue());
        if (selectBirthList.size() > 0) {
            for (BirthDay birthDay : selectBirthList) {
                sendMessage.setChatId(birthDay.getUser().getChatId());
                sendMessage.setText("Bugun " + birthDay.getFullName() + " do'stingizning tug'ilgan kuni! tabriklab qo'yishni unutmang!");
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Xabar yuborildi!");
        }
    }


    private SendMessage myImportMessage(long chatId, CallbackQuery callbackQuery){
        uz.real.appbotsecond.model.User user = userRepository.getByUsername(callbackQuery.getFrom().getUserName());
        int pageNumber = 0;
        int pageSize = 2;
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("id").descending());
        Page<BirthDay> pagedResult = birthDayRepository.findAllByUserId_OrderByCreatedAtDesc(user.getId(), pageable);
        if (!pagedResult.hasContent()){
         return new SendMessage()
                 .setChatId(chatId)
                 .setText("No content!");
        }
        for (BirthDay birthDay : pagedResult.getContent()) {
            return new SendMessage()
                    .setChatId(chatId)
                    .setText(String.valueOf(new ResBirthDay(birthDay.getFullName(), birthDay.getBirthDayDate())));
        }  return new SendMessage()
                .setChatId(chatId)
                .setText("No content!");

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
