package com.parkit.parkingsystem.integration;


import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/*
 *  classe de test d’intégration
 *  vérifie que l’intégration entre les différentes couches fonctionne correctement
 */
public class ParkingDataBaseIT {

    private static ParkingService parkingService;
    private static DataBasePrepareService dataBasePrepareService;
    private static TicketDAO ticketDAO;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    
    @BeforeAll
    public static void setUp() {
        inputReaderUtil = mock(InputReaderUtil.class);
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = new DataBaseTestConfig();
        ParkingSpotDAO parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = new DataBaseTestConfig();
        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        dataBasePrepareService = new DataBasePrepareService();
    }

    
    @BeforeEach
    public void setUpPerTest() {
        dataBasePrepareService.clearDataBaseEntries();
    }

    
    //tester l’entrée d’un véhicule (création du ticket en base de données).
    @Test
    public void testParkingACar() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1); // CAR
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

        parkingService.processIncomingVehicle();

        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(ticket);
        assertNull(ticket.getOutTime());
        assertEquals("ABCDEF", ticket.getVehicleRegNumber());
    }

    
    
    //tester la sortie d’un véhicule et le calcul du tarif.
    @Test
    public void testParkingLotExit() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1); // CAR
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

        parkingService.processIncomingVehicle();

        
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        ticket.setInTime(new Date(System.currentTimeMillis() - 60 * 60 * 1000));
       
        ticketDAO.updateInTime(ticket);

        parkingService.processExitingVehicle();

        Ticket updatedTicket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(updatedTicket.getOutTime(), "Le champ outTime doit être renseigné à la sortie");
        assertTrue(updatedTicket.getPrice() > 0, "Le prix doit être supérieur à 0");
    }

    
    
    //tester le calcul du tarif avec remise de 5% pour un utilisateur récurrent.
    @Test
    public void testParkingLotExitRecurringUser() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1); // CAR
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

        // Premier passage
        parkingService.processIncomingVehicle();
        Ticket ticket1 = ticketDAO.getTicket("ABCDEF");
        ticket1.setInTime(new Date(System.currentTimeMillis() - 60 * 60 * 1000));
       
        ticketDAO.updateInTime(ticket1);
        parkingService.processExitingVehicle();

        // Deuxième passage (récurrent)
        parkingService.processIncomingVehicle();
        Ticket ticket2 = ticketDAO.getTicket("ABCDEF");
        ticket2.setInTime(new Date(System.currentTimeMillis() - 60 * 60 * 1000));
        
        ticketDAO.updateInTime(ticket2);
        parkingService.processExitingVehicle();

        Ticket updatedTicket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(updatedTicket.getOutTime(), "Le champ outTime doit être renseigné à la sortie (2e sortie)");
        assertTrue(updatedTicket.getPrice() > 0, "Le prix doit être supérieur à 0");
        assertTrue(updatedTicket.getPrice() < 1.5, "Le prix devrait refléter une remise de 5%");
    }
    
    
}
