package uz.real.appbotsecond.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResBirthDay {

    private String fullName;
    private LocalDate birthDayDate;


}
