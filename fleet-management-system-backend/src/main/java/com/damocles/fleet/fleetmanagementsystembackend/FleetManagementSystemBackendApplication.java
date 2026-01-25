package com.damocles.fleet.fleetmanagementsystembackend;

import com.damocles.fleet.fleetmanagementsystembackend.config.CorsProperties;
import lombok.extern.java.Log;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.client.RestTemplate;
import org.springframework.jdbc.core.JdbcTemplate;



import javax.sql.DataSource;
import java.sql.SQLException;

@SpringBootApplication
@Log
@EnableConfigurationProperties(CorsProperties.class)
public class FleetManagementSystemBackendApplication  {



    public static void main(String[] args) {
        SpringApplication.run(FleetManagementSystemBackendApplication.class, args);
    }


}
