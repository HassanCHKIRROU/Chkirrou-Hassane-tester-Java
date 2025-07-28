package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.model.ParkingSpot;

//calculer le tarif du ticket de stationnement.	
public class FareCalculatorService {

	 
	    
	  //ajout de parametre discount pour appliquer la remise de 5% pour les utilisateurs reguliers
      /*
       * Calcule le tarif à payer en fonction du temps de stationnement,
       * du type de véhicule et d'une éventuelle réduction (utilisateur régulier).
	    
	   */
	    public void calculateFare(Ticket ticket, boolean discount) {
	        if (ticket.getOutTime() == null || ticket.getOutTime().before(ticket.getInTime())) {
	            throw new IllegalArgumentException("Out time is incorrect: " + ticket.getOutTime());
	        }
	        /**
	         * Correction du bug par remplacement de l'heure d'entrée et de sortie de: Hour --> Millisecondes
	         * 
	         * duration en heure
	         */
	        

	        long inMillis = ticket.getInTime().getTime();
	        long outMillis = ticket.getOutTime().getTime();
	        double durationInHours = (outMillis - inMillis) / (1000.0 * 60 * 60); // conversion Millisecondes en  Heures

	        if (durationInHours < 0.5) {
	            ticket.setPrice(0.0); // gratuité pour des parkings < 30 minutes
	            return;
	        }

	        ParkingSpot parkingSpot = ticket.getParkingSpot();
	        if (parkingSpot == null || parkingSpot.getParkingType() == null) {
	            throw new IllegalArgumentException("Parking type is unknown");
	        }

	        double ratePerHour;
	        switch (parkingSpot.getParkingType()) {
	            case CAR:
	                ratePerHour = Fare.CAR_RATE_PER_HOUR;
	                break;
	            case BIKE:
	                ratePerHour = Fare.BIKE_RATE_PER_HOUR;
	                break;
	            default:
	                throw new IllegalArgumentException("Unknown Parking Type");
	        }
	      //Implementation de la fonctionnalité de la remise de 5% pour les utilisateurs réguliers

	        double price = durationInHours * ratePerHour;

	        if (discount) {
	            price *= 0.95; // 5 % de remise
	        }

	       
	        ticket.setPrice(price);
	    }
	

	
    
    //surcharge de la methode calculateFare avec le parametre discount= false par defaut
    public void calculateFare(Ticket ticket) {
    	calculateFare(ticket, false);
    }
    
}