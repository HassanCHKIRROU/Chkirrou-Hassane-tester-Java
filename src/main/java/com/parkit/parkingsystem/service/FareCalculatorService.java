package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket){
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
    }
    
    
}