package uz.real.appbotsecond.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uz.real.appbotsecond.model.BirthDay;
import uz.real.appbotsecond.model.User;

import java.util.List;

@Repository
public interface BirthDayRepository extends JpaRepository<BirthDay, Long> {

    List<BirthDay> findAllByUser(User user);


   @Query(value="select * from birth b where b.birth_date.day =?1 and b.birth_date.monthValue= ?2", nativeQuery = true)
    List<BirthDay> find(int day, int month);

    Page<BirthDay> findAllByUserId_OrderByCreatedAtDesc(Long userId, Pageable pageable);


}
