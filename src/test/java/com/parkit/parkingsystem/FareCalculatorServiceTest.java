package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

//teste la classe FareCalculatorService, responsable du calcul du tarif de parking.
public class FareCalculatorServiceTest {

    private FareCalculatorService fareCalculatorService;
    private Ticket ticket;

    
    //Initialisation avant chaque test
    @BeforeEach
    public void setUp() {
        fareCalculatorService = new FareCalculatorService();
        ticket = new Ticket();
    }

    
    //Vérifie que le tarif d’une voiture pour 1h est correct
    @Test
    public void calculateFareCar_OneHour() {
        Date in = new Date();
        in.setTime(System.currentTimeMillis() - (60 * 60 * 1000)); // 1h
        Date out = new Date();

        ParkingSpot spot = new ParkingSpot(1, ParkingType.CAR, false);
        ticket.setParkingSpot(spot);
        ticket.setInTime(in);
        ticket.setOutTime(out);

        fareCalculatorService.calculateFare(ticket, false);

        assertEquals(Fare.CAR_RATE_PER_HOUR, ticket.getPrice(), 0.001); //  delta ajouté
    }

    
    //Vérifie que le tarif d’une moto pour 1h est correct
    @Test
    public void calculateFareBike_OneHour() {
        Date in = new Date();
        in.setTime(System.currentTimeMillis() - (60 * 60 * 1000)); // 1h
        Date out = new Date();

        ParkingSpot spot = new ParkingSpot(1, ParkingType.BIKE, false);
        ticket.setParkingSpot(spot);
        ticket.setInTime(in);
        ticket.setOutTime(out);

        fareCalculatorService.calculateFare(ticket, false);

        assertEquals(Fare.BIKE_RATE_PER_HOUR, ticket.getPrice(), 0.001); // delta ajouté
    }

    
    //Test de la fonctionnalité de gratuité des parking <30 min (voiture) 
    @Test
    public void calculateFareCarWithLessThan30MinutesParkingTime() {
        Date in = new Date();
        in.setTime(System.currentTimeMillis() - (29 * 60 * 1000)); // 29 min
        Date out = new Date();

        ParkingSpot spot = new ParkingSpot(1, ParkingType.CAR, false);
        ticket.setParkingSpot(spot);
        ticket.setInTime(in);
        ticket.setOutTime(out);

        fareCalculatorService.calculateFare(ticket, false);

        assertEquals(0.0, ticket.getPrice(), 0.001); 
    }
    
    
  //Test de la fonctionnalité de gratuité des parking <30 min (moto)
    @Test
    public void calculateFareBikeWithLessThan30MinutesParkingTime() {
        Date in = new Date();
        in.setTime(System.currentTimeMillis() - (29 * 60 * 1000)); // 29 min
        Date out = new Date();

        ParkingSpot spot = new ParkingSpot(1, ParkingType.BIKE, false);
        ticket.setParkingSpot(spot);
        ticket.setInTime(in);
        ticket.setOutTime(out);

        fareCalculatorService.calculateFare(ticket, false);

        assertEquals(0.0, ticket.getPrice(), 0.001); //  delta ajouté
    }

    
    //Test de la fonctionnalité de remise de 5% pour les utilisateurs réguliers (voiture)
    @Test
    public void calculateFareCarWithDiscount() {
        Date in = new Date();
        in.setTime(System.currentTimeMillis() - (60 * 60 * 1000)); // 1h
        Date out = new Date();

        ParkingSpot spot = new ParkingSpot(1, ParkingType.CAR, false);
        ticket.setParkingSpot(spot);
        ticket.setInTime(in);
        ticket.setOutTime(out);

        fareCalculatorService.calculateFare(ticket, true);

        assertEquals(Fare.CAR_RATE_PER_HOUR * 0.95, ticket.getPrice(), 0.001); //  delta ajouté
    }
    
   //Test de la fonctionnalité de remise de 5% pour les utilisateurs réguliers (voiture)
    @Test
    public void calculateFareBikeWithDiscount() {
        Date in = new Date();
        in.setTime(System.currentTimeMillis() - (60 * 60 * 1000)); // 1h
        Date out = new Date();

        ParkingSpot spot = new ParkingSpot(1, ParkingType.BIKE, false);
        ticket.setParkingSpot(spot);
        ticket.setInTime(in);
        ticket.setOutTime(out);

        fareCalculatorService.calculateFare(ticket, true);

        assertEquals(Fare.BIKE_RATE_PER_HOUR * 0.95, ticket.getPrice(), 0.001); //  delta ajouté
    }
    

    //test pour le type de véhicule inconnu
    @Test
    public void calculateFareUnknownType_ShouldThrowException() {
        Date in = new Date();
        in.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
        Date out = new Date();

        ParkingSpot spot = new ParkingSpot(1, null, false); //parkingType = null
        ticket.setParkingSpot(spot);
        ticket.setInTime(in);
        ticket.setOutTime(out);

        assertThrows(IllegalArgumentException.class, () -> {
            fareCalculatorService.calculateFare(ticket, false);
        });
    }
    
   
   //Verifie que le systeme rejete un ticket sans heure de sortie
    @Test
    public void calculateFare_NullOutTime_ShouldThrowException() {
        ticket.setInTime(new Date());
        ticket.setOutTime(null); // pas de sortie
        ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));

        assertThrows(IllegalArgumentException.class, () -> {
            fareCalculatorService.calculateFare(ticket, false);
        });
    }
    

    //verifie que le l'heure d'entrée est bien en avant de l'heure de sortie
    @Test
    public void calculateFare_OutTimeBeforeInTime_ShouldThrowException() {
        Date in = new Date();
        Date out = new Date();
        out.setTime(System.currentTimeMillis() - (60 * 60 * 1000)); // 1h avant maintenant

        ticket.setInTime(in);
        ticket.setOutTime(out);
        ticket.setParkingSpot(new ParkingSpot(1, ParkingType.BIKE, false));

        assertThrows(IllegalArgumentException.class, () -> {
            fareCalculatorService.calculateFare(ticket, false);
        });
    }
    
}
