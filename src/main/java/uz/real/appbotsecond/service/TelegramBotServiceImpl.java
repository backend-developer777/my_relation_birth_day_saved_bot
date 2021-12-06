package uz.real.appbotsecond.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import uz.real.appbotsecond.model.BirthDay;
import uz.real.appbotsecond.payload.ResBirthDay;
import uz.real.appbotsecond.repository.BirthDayRepository;
import uz.real.appbotsecond.repository.UserRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TelegramBotServiceImpl implements TelegramBotService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BirthDayRepository birthDayRepository;

    @Override
    public SendMessage useBot(long chatId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText("Muhim sanalarni qo'shish!");
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

    @Override
    public SendMessage saveUser(CallbackQuery callbackQuery, long chatId) {
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

    @Override
    public SendMessage checkDate(String stringDate, Message message, String fullName, long birthId) {
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        int day = Integer.parseInt(stringDate.substring(0,2));
        int month = Integer.parseInt(stringDate.substring(3, 5));
        int year = Integer.parseInt(stringDate.substring(6, stringDate.length()));

        if (day >= 1 && day <= 31) {
            if (month >= 1 && month <= 12) {
                if (year > LocalDate.now().getYear() - 120 && year < LocalDate.now().getYear()) {
                    LocalDate birthDayDate = LocalDate.parse(stringDate, formatter);
                    if (birthId != 0) {
                        Optional<BirthDay> byId = birthDayRepository.findById(birthId);
                        if (byId.isPresent()) {
                            BirthDay birthDay = byId.get();
                            birthDay.setId(birthId);
                            birthDay.setFullName(fullName);
                            birthDay.setBirthDayDate(birthDayDate);
                            birthDay.setUser(userRepository.getByUsername(message.getFrom().getUserName()));
                            birthDayRepository.save(birthDay);
                            return new SendMessage()
                                    .setChatId(message.getChatId())
                                    .setText("Muvafaqqiyatli o'zgartirildi!" + "\n Ismi va familyasi:  " + birthDay.getFullName() +"\nTug'ilgan sanasi: " + birthDay.getBirthDayDate() + " ga o'zgardi!")
                                    .setReplyMarkup(crudAndPaginationButtons(birthId));
                        }
                    }
                    BirthDay savedBirthDay = new BirthDay();
                    savedBirthDay.setBirthDayDate(birthDayDate);
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
                            .setText("Yilni to'g'ri kiriting, yil "+ (LocalDate.now().getYear()+1) + " dan kichik bo'lishi kerak. Siz " + year + " kiritdingiz!");
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

    @Override
    public SendMessage myImportantMessage(long chatId, CallbackQuery callbackQuery) {
        User from = callbackQuery.getFrom();
        String userName = from.getUserName();
        if (userName != null) {
            Optional<uz.real.appbotsecond.model.User> byUsername = userRepository.findByUsername(from.getUserName());
            if (byUsername.isPresent()) {
                uz.real.appbotsecond.model.User user = byUsername.get();
                List<BirthDay> birthDayList = birthDayRepository.getAllByUserId(user.getId());
                if (birthDayList.size() > 0) {
                    BirthDay birthDay = birthDayList.get(birthDayList.size() - 1);
                    long currentId = birthDay.getId();
                    InlineKeyboardMarkup keyboardMarkup = crudAndPaginationButtons(currentId);
                    return new SendMessage()
                            .setChatId(chatId)
                            .setText("Oxirgi qo'shgan do'stingizning tug'ilgan sanasi = " + birthDay.getBirthDayDate() + "  Ismi va familyasi = " + birthDay.getFullName())
                            .setReplyMarkup(keyboardMarkup);
                }
            }
        }
        return new SendMessage()
                .setChatId(chatId)
                .setText("Siz hozircha do'stlaringizni tug'ilgan kunini saqlamagansiz, 'Muhim sanalarni qo'shish!' tugmachasini tanlang!");
    }

    @Override
    @Transactional
    public SendMessage prev(String birthId, long chatId, CallbackQuery callbackQuery) {
        long currentBirthDayId = Long.parseLong(birthId.substring(1));
        User from = callbackQuery.getFrom();
        String userName = from.getUserName();
        Optional<uz.real.appbotsecond.model.User> byUsername = userRepository.findByUsername(userName);
        if (byUsername.isPresent()) {
            uz.real.appbotsecond.model.User user = byUsername.get();
            List<BirthDay> allByUserId = birthDayRepository.getAllByUserId(user.getId());
            long firstBirthDayId = allByUserId.get(0).getId();
            long lastBirthDayId = allByUserId.get(allByUserId.size() - 1).getId();
            if (currentBirthDayId > firstBirthDayId && currentBirthDayId <= lastBirthDayId) {
                long prev = currentBirthDayId - 1;
                Optional<BirthDay> birthDayOptional = birthDayRepository.findById(prev);
                if (birthDayOptional.isPresent()) {
                    BirthDay birthDay = birthDayOptional.get();
                    return new SendMessage()
                            .setChatId(chatId)
                            .setText("Tug'ilgan sanasi:  " + birthDay.getBirthDayDate() + " \n  Ismi va familyasi:  " + birthDay.getFullName())
                            .setReplyMarkup(crudAndPaginationButtons(prev));
                } else {
                    while (firstBirthDayId != prev) {
                        prev--;
                        Optional<BirthDay> byId = birthDayRepository.findById(prev);
                        if (byId.isPresent()) {
                            return new SendMessage()
                                    .setChatId(chatId)
                                    .setText("Tug'ilgan sanasi:  " + byId.get().getBirthDayDate() + "\n Ismi va familyasi:  " + byId.get().getFullName())
                                    .setReplyMarkup(crudAndPaginationButtons(prev));
                        }
                    }
                    return new SendMessage()
                            .setChatId(chatId)
                            .setText("Tug'ilgan sanasi: " + birthDayRepository.findById(firstBirthDayId).get().getBirthDayDate() + "\n Ismi va familyasi:  " + birthDayRepository.findById(firstBirthDayId).get().getFullName())
                            .setReplyMarkup(crudAndPaginationButtons(firstBirthDayId));
                }
            } else {
                Optional<BirthDay> birthDayOptional = birthDayRepository.findById(currentBirthDayId);
                if (birthDayOptional.isPresent()){
                    BirthDay birthDay = birthDayOptional.get();
                    return new SendMessage()
                            .setChatId(chatId)
                            .setText("Ismi va familyasi: " + birthDay.getFullName() + "\nTug'ilgan sanasi:  " + birthDay.getBirthDayDate())
                            .setReplyMarkup(firstBirthDayBTN(currentBirthDayId));
                }

            }
        }
            return new SendMessage()
                    .setChatId(chatId)
                    .setText("Siz hamma do'stlaringizni o'chirib bo'lgansiz! /start");

    }

    private InlineKeyboardMarkup firstBirthDayBTN(long birthId){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton3 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButtonOld1 = new InlineKeyboardButton();
        inlineKeyboardButtonOld1.setText("Keyingi >>");
        inlineKeyboardButtonOld1.setCallbackData("n" + birthId);
        inlineKeyboardButton.setText("O'chirish!");
        inlineKeyboardButton.setCallbackData("d" + birthId);
        inlineKeyboardButton1.setText("Qo'shish!");
        inlineKeyboardButton1.setCallbackData("add");
        inlineKeyboardButton2.setText("O'zgartirish!");
        inlineKeyboardButton2.setCallbackData("e" + birthId);
        inlineKeyboardButton3.setText("Barchasini ko'rish!");
        inlineKeyboardButton3.setCallbackData("view");
        List<InlineKeyboardButton> keyboardButtonList = new LinkedList<>();
        List<InlineKeyboardButton> keyboardButtonListOld = new LinkedList<>();
        List<InlineKeyboardButton> keyboardButtonListEditAndView = new LinkedList<>();
        keyboardButtonListOld.add(inlineKeyboardButtonOld1);
        keyboardButtonList.add(inlineKeyboardButton);
        keyboardButtonList.add(inlineKeyboardButton1);
        keyboardButtonListEditAndView.add(inlineKeyboardButton2);
        keyboardButtonListEditAndView.add(inlineKeyboardButton3);
        List<List<InlineKeyboardButton>> inlineRows = new LinkedList<>();
        inlineRows.add(keyboardButtonListOld);
        inlineRows.add(keyboardButtonList);
        inlineRows.add(keyboardButtonListEditAndView);

        return inlineKeyboardMarkup.setKeyboard(inlineRows);
    }

    @Override
    public SendMessage next(String birthId, long chatId, CallbackQuery callbackQuery) {
        long currentBirthId = Long.parseLong(birthId.substring(1));
        List<BirthDay> allByUser_username = birthDayRepository.findAllByUser_Username(callbackQuery.getFrom().getUserName());
        long lastBirthId = allByUser_username.get(allByUser_username.size() - 1).getId();
        if (currentBirthId < lastBirthId) {
            long nextBirthId = currentBirthId + 1;
            Optional<BirthDay> birthDayOptional = birthDayRepository.findById(nextBirthId);
            if (birthDayOptional.isPresent()) {
                BirthDay birthDay = birthDayOptional.get();
                return new SendMessage()
                        .setChatId(chatId)
                        .setText("Tug'ilgan sanasi: " + birthDay.getBirthDayDate() + "\n + Ismi va familiyasi: " + birthDay.getFullName())
                        .setReplyMarkup(crudAndPaginationButtons(nextBirthId));
            } else {
                while (nextBirthId != lastBirthId) {
                    nextBirthId++;
                    Optional<BirthDay> byId = birthDayRepository.findById(nextBirthId);
                    if (byId.isPresent()) {
                        return new SendMessage()
                                .setChatId(chatId)
                                .setText("Tug'ilgan sanasi:  " + byId.get().getBirthDayDate() + "\n Ismi va familiyasi:  " + byId.get().getFullName())
                                .setReplyMarkup(crudAndPaginationButtons(nextBirthId));
                    }
                }
            }
        } else if (currentBirthId == lastBirthId) {
            Optional<BirthDay> birthDayNextOptional = birthDayRepository.findById(currentBirthId);
            if (birthDayNextOptional.isPresent()){
                BirthDay birthDay = birthDayNextOptional.get();
                return new SendMessage()
                        .setChatId(chatId)
                        .setText("Tug'ilgan sanasi:  " +birthDay.getBirthDayDate() + "\nIsmi va familiyasi:  " + birthDay.getFullName())
                        .setReplyMarkup(lastBirthDayBTN(currentBirthId));
            }

        }
        return null;
    }

    private InlineKeyboardMarkup lastBirthDayBTN(long birthId){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton3 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButtonOld1 = new InlineKeyboardButton();
        inlineKeyboardButtonOld1.setText("<< Oldingi");
        inlineKeyboardButtonOld1.setCallbackData("p" + birthId);
        inlineKeyboardButton.setText("O'chirish!");
        inlineKeyboardButton.setCallbackData("d" + birthId);
        inlineKeyboardButton1.setText("Qo'shish!");
        inlineKeyboardButton1.setCallbackData("add");
        inlineKeyboardButton2.setText("O'zgartirish!");
        inlineKeyboardButton2.setCallbackData("e" + birthId);
        inlineKeyboardButton3.setText("Barchasini ko'rish!");
        inlineKeyboardButton3.setCallbackData("view");
        List<InlineKeyboardButton> keyboardButtonList = new LinkedList<>();
        List<InlineKeyboardButton> keyboardButtonListOld = new LinkedList<>();
        List<InlineKeyboardButton> keyboardButtonListEditAndView = new LinkedList<>();
        keyboardButtonListOld.add(inlineKeyboardButtonOld1);
        keyboardButtonList.add(inlineKeyboardButton);
        keyboardButtonList.add(inlineKeyboardButton1);
        keyboardButtonListEditAndView.add(inlineKeyboardButton2);
        keyboardButtonListEditAndView.add(inlineKeyboardButton3);
        List<List<InlineKeyboardButton>> inlineRows = new LinkedList<>();
        inlineRows.add(keyboardButtonListOld);
        inlineRows.add(keyboardButtonList);
        inlineRows.add(keyboardButtonListEditAndView);

        return inlineKeyboardMarkup.setKeyboard(inlineRows);


    }

    @Override
    public SendMessage add(long chatId) {
        return new SendMessage()
                .setChatId(chatId)
                .setText("Ism va familyangizni kiriting kiriting! Masalan Sherzod Nurmatov ko'rinishida!");
    }

    @Override
    public SendMessage edit(String birthId, long chatId) {
        long currentBirthId = Long.parseLong(birthId.substring(1));
        BirthDay birthDay = birthDayRepository.findById(currentBirthId).get();
        return new SendMessage()
                .setChatId(chatId)
                .setText(birthDay.getFullName() + " ni o'zgartirmoqchisiz, yangi ism va familiya kiriting!");
    }

    @Override
    public SendMessage delete(String birthId, long chatId) {
        long currentBirthId = Long.parseLong(birthId.substring(1));
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        inlineKeyboardButton.setText("Ha");
        inlineKeyboardButton.setCallbackData("Ha" + currentBirthId);
        inlineKeyboardButton1.setText("Yo'q");
        inlineKeyboardButton1.setCallbackData("Yo" + currentBirthId);
        List<InlineKeyboardButton> keyboardButtonList = new LinkedList<>();
        keyboardButtonList.add(inlineKeyboardButton);
        keyboardButtonList.add(inlineKeyboardButton1);
        List<List<InlineKeyboardButton>> inlineRows = new LinkedList<>();
        inlineRows.add(keyboardButtonList);
        inlineKeyboardMarkup.setKeyboard(inlineRows);
        if (birthDayRepository.findById(currentBirthId).isPresent()) {
            return new SendMessage()
                    .setChatId(chatId)
                    .setText("Rostan ham o'chirmoqchimisiz!")
                    .setReplyMarkup(inlineKeyboardMarkup);
        }
        return new SendMessage()
                .setChatId(chatId)
                .setText("Belgilagan do'stlaringizni hammasini o'chirib bo'ldingiz!")
                .setReplyMarkup(crudAndPaginationButtons(currentBirthId));

    }

    @Override
    public SendMessage successDelete(String birthId, long chatId) {
        long currentBirthId = Long.parseLong(birthId.substring(2));
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Orqaga!");
        button.setCallbackData("Orqaga!");
        List<InlineKeyboardButton> inlineKeyboardButtonList = new LinkedList<>();
        inlineKeyboardButtonList.add(button);
        List<List<InlineKeyboardButton>> inlineRows = new LinkedList<>();
        inlineRows.add(inlineKeyboardButtonList);
        inlineKeyboardMarkup.setKeyboard(inlineRows);
        birthDayRepository.deleteById(currentBirthId);
        return new SendMessage()
                .setChatId(chatId)
                .setText("Muvafaqqiyatli o'chirildi!")
                .setReplyMarkup(inlineKeyboardMarkup);
    }

    @Override
    public SendMessage notDelete(String birthId, long chatId) {
        long currentBirthId = Long.parseLong(birthId.substring(2));
        return new SendMessage()
                .setChatId(chatId)
                .setText("Amaliyot bajarilmadi!")
                .setReplyMarkup(crudAndPaginationButtons(currentBirthId));
    }

    @Override
    public SendMessage viewAll(CallbackQuery callbackQuery, long chatId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText("Orqaga!");
        inlineKeyboardButton.setCallbackData("Orqaga!");
        List<InlineKeyboardButton> keyboardButtonList = new LinkedList<>();
        keyboardButtonList.add(inlineKeyboardButton);
        List<List<InlineKeyboardButton>> inlineRows = new LinkedList<>();
        inlineRows.add(keyboardButtonList);
        inlineKeyboardMarkup.setKeyboard(inlineRows);
        String userName = callbackQuery.getFrom().getUserName();
        List<BirthDay> allByUser_username = birthDayRepository.findAllByUser_Username(userName);
        if (allByUser_username.size() > 0) {
            List<ResBirthDay> resBirthDayList = new LinkedList<>();
            for (BirthDay birthDay : allByUser_username) {
                resBirthDayList.add(new ResBirthDay(birthDay.getFullName(), birthDay.getBirthDayDate()));
            }
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText(""+resBirthDayList.stream().map(resBirthDay -> {
                return "Ismi va familyasi:   " + resBirthDay.getFullName() + " \nTug'ilgan sanasi:   " + resBirthDay.getBirthDayDate() + "\n\n";
            }).collect(Collectors.joining()));
            sendMessage.setReplyMarkup(inlineKeyboardMarkup);
            return sendMessage;

        }
        return new SendMessage()
                .setChatId(chatId)
                .setText("Avval do'stlaringizni belgilang!")
                .setReplyMarkup(inlineKeyboardMarkup);

    }

    private InlineKeyboardMarkup crudAndPaginationButtons(long birthId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton3 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButtonOld1 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButtonOld2 = new InlineKeyboardButton();
        inlineKeyboardButtonOld1.setText("<< Oldingi");
        inlineKeyboardButtonOld1.setCallbackData("p" + birthId);
        inlineKeyboardButtonOld2.setText("Keyingi >>");
        inlineKeyboardButtonOld2.setCallbackData("n" + birthId);
        inlineKeyboardButton.setText("O'chirish!");
        inlineKeyboardButton.setCallbackData("d" + birthId);
        inlineKeyboardButton1.setText("Qo'shish!");
        inlineKeyboardButton1.setCallbackData("add");
        inlineKeyboardButton2.setText("O'zgartirish!");
        inlineKeyboardButton2.setCallbackData("e" + birthId);
        inlineKeyboardButton3.setText("Barchasini ko'rish!");
        inlineKeyboardButton3.setCallbackData("view");
        List<InlineKeyboardButton> keyboardButtonList = new LinkedList<>();
        List<InlineKeyboardButton> keyboardButtonListOld = new LinkedList<>();
        List<InlineKeyboardButton> keyboardButtonListEditAndView = new LinkedList<>();
        keyboardButtonListOld.add(inlineKeyboardButtonOld1);
        keyboardButtonListOld.add(inlineKeyboardButtonOld2);
        keyboardButtonList.add(inlineKeyboardButton);
        keyboardButtonList.add(inlineKeyboardButton1);
        keyboardButtonListEditAndView.add(inlineKeyboardButton2);
        keyboardButtonListEditAndView.add(inlineKeyboardButton3);
        List<List<InlineKeyboardButton>> inlineRows = new LinkedList<>();
        inlineRows.add(keyboardButtonListOld);
        inlineRows.add(keyboardButtonList);
        inlineRows.add(keyboardButtonListEditAndView);

        return inlineKeyboardMarkup.setKeyboard(inlineRows);
    }

}
