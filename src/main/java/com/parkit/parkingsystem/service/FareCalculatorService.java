package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {
	
          //ajout de parametre discount pour appliquer la remise de 5% pour les utilisateurs reguliers
    public void calculateFare(Ticket ticket, boolean discount){  
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }
       
        
        /**
         * Correction du bug par remplacement de l'heure d'entrée et de sortie en Hour --> Millisecondes
         * 
         * duration en heure
         */
        
        long durationInMillis = ticket.getOutTime().getTime() - ticket.getInTime().getTime();
        double durationInHours = (double)durationInMillis/(60*60*1000);
  
       /*
        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                ticket.setPrice(durationInHours * Fare.CAR_RATE_PER_HOUR);
                break;
            }
            case BIKE: {
                ticket.setPrice(durationInHours * Fare.BIKE_RATE_PER_HOUR);
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }
        
        // Parking gratuit si la durée < 30 min
       
        if(durationInHours < 0.5) {
        	ticket.setPrice(0.0);
        	return;
        }
        
        switch(ticket.getParkingSpot().getParkingType()) {
           case CAR:{
        	  ticket.setPrice(durationInHours * Fare.CAR_RATE_PER_HOUR);
        	  break;
        }
        
          case BIKE:{
        	  ticket.setPrice(durationInHours * Fare.BIKE_RATE_PER_HOUR);
        	  break;
        }
          default:
        	  throw new IllegalArgumentException("UnKnown Parking TYpe");
        }  
   */
    
    //Implementer la fonctionnalité de la remise de 5% pour les utilisateyrs réguliers
    
    double ratePerHour;
    
    
    switch(ticket.getParkingSpot().getParkingType()) {
    
    case CAR:
    	ratePerHour = Fare.CAR_RATE_PER_HOUR;
    	break;
    	
    case BIKE:
    	ratePerHour = Fare.BIKE_RATE_PER_HOUR;
    		break;
    		
    default:
    	throw new IllegalArgumentException("Unknown Parking Type");
    }
    
    
    double price = durationInHours * ratePerHour;
    
    //apliquer la remise si necessaire
    
    if(discount) {
    	price *= 0.95;
    }
    
    ticket.setPrice(price);
    
    }
    
    //surcharge de la methode calculateFare avec le parametre discount= false par defaut
    public void calculateFare(Ticket ticket) {
    	calculateFare(ticket, false);
    }
    
}