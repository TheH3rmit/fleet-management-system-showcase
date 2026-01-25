package com.damocles.fleet.fleetmanagementsystembackend.support;

import com.damocles.fleet.fleetmanagementsystembackend.domain.Account;
import com.damocles.fleet.fleetmanagementsystembackend.domain.AccountStatus;
import com.damocles.fleet.fleetmanagementsystembackend.domain.Driver;
import com.damocles.fleet.fleetmanagementsystembackend.domain.DriverStatus;
import com.damocles.fleet.fleetmanagementsystembackend.domain.Location;
import com.damocles.fleet.fleetmanagementsystembackend.domain.Trailer;
import com.damocles.fleet.fleetmanagementsystembackend.domain.TrailerStatus;
import com.damocles.fleet.fleetmanagementsystembackend.domain.User;
import com.damocles.fleet.fleetmanagementsystembackend.domain.UserRole;
import com.damocles.fleet.fleetmanagementsystembackend.domain.Vehicle;
import com.damocles.fleet.fleetmanagementsystembackend.domain.VehicleStatus;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IAccountRepository;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IDriverRepository;
import com.damocles.fleet.fleetmanagementsystembackend.repository.ILocationRepository;
import com.damocles.fleet.fleetmanagementsystembackend.repository.ITrailerRepository;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IUserRepository;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IVehicleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class TestDataFactory {

    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    private final IUserRepository userRepository;
    private final IAccountRepository accountRepository;
    private final IDriverRepository driverRepository;
    private final ILocationRepository locationRepository;
    private final IVehicleRepository vehicleRepository;
    private final ITrailerRepository trailerRepository;
    private final PasswordEncoder passwordEncoder;

    public TestDataFactory(
            IUserRepository userRepository,
            IAccountRepository accountRepository,
            IDriverRepository driverRepository,
            ILocationRepository locationRepository,
            IVehicleRepository vehicleRepository,
            ITrailerRepository trailerRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.driverRepository = driverRepository;
        this.locationRepository = locationRepository;
        this.vehicleRepository = vehicleRepository;
        this.trailerRepository = trailerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(String suffix) {
        String safe = suffix == null || suffix.isBlank()
                ? "u" + COUNTER.incrementAndGet()
                : suffix;

        User user = new User();
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail(safe + "@local");
        user.setPhone("000000000");
        return userRepository.save(user);
    }

    public Account createAccount(String login, String rawPassword, Set<UserRole> roles) {
        User user = createUser(login);

        Account acc = new Account();
        acc.setLogin(login);
        acc.setPasswordHash(passwordEncoder.encode(rawPassword));
        acc.setStatus(AccountStatus.ACTIVE);
        acc.setRoles(roles);
        acc.setUser(user);

        user.setAccount(acc);
        return accountRepository.save(acc);
    }

    public Driver createDriver(User user, String licenseNumber) {
        Driver driver = new Driver();
        driver.setUser(user);
        driver.setUserId(user.getId());
        driver.setDriverLicenseNumber(licenseNumber);
        driver.setDriverLicenseCategory("B");
        driver.setDriverLicenseExpiryDate(LocalDate.now().plusYears(1));
        driver.setDriverStatus(DriverStatus.AVAILABLE);
        return driverRepository.save(driver);
    }

    public Location createLocation(String suffix) {
        String safe = suffix == null || suffix.isBlank()
                ? "L" + COUNTER.incrementAndGet()
                : suffix;

        Location loc = new Location();
        loc.setCity("City" + safe);
        loc.setStreet("Main");
        loc.setBuildingNumber("1");
        loc.setPostcode("00-000");
        loc.setCountry("PL");
        return locationRepository.save(loc);
    }

    public Vehicle createVehicle(String plate) {
        Vehicle v = new Vehicle();
        v.setManufacturer("Test");
        v.setModel("Model");
        v.setLicensePlate(plate);
        v.setVehicleStatus(VehicleStatus.ACTIVE);
        v.setAllowedLoad(1000);
        return vehicleRepository.save(v);
    }

    public Trailer createTrailer(String plate) {
        Trailer t = new Trailer();
        t.setName("Trailer");
        t.setLicensePlate(plate);
        t.setPayload(new BigDecimal("1000"));
        t.setVolume(new BigDecimal("10"));
        t.setTrailerStatus(TrailerStatus.ACTIVE);
        return trailerRepository.save(t);
    }

}
