package uz.real.appbotsecond.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uz.real.appbotsecond.model.BirthDay;
import uz.real.appbotsecond.model.User;

import javax.xml.transform.sax.SAXTransformerFactory;
import java.util.List;

@Repository
public interface BirthDayRepository extends JpaRepository<BirthDay, Long> {

    List<BirthDay> findAllByUser(User user);


   @Query(value="select * from birth b where :month = date_part('month', b.birth_date) and :day = date_part('day', b.birth_date)", nativeQuery=true)
    List<BirthDay> find(int month, int day);

    List<BirthDay> getAllByUserId(Long userId);

    List<BirthDay> findAllByUser_Username(String username);




}
