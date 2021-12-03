package uz.real.appbotsecond.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.real.appbotsecond.model.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    User getByUsername(String username);

    // 500 X.A

    // 400 KV.

    // 300 K.K

    // 300 K.U

    // 100$ Z.F

    // 100 $ Y.K.Y

    // hp Intel seleron 2.2GZ Operativka 2GB






}
