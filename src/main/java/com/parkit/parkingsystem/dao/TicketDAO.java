package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
/*
 * permet d'effectuer toutes les opérations sur les tickets de parking dans la base de données (table ticket)
 * enregistrement, récupération, mise à jour, comptage.

 */
public class TicketDAO {

    private static final Logger logger = LogManager.getLogger("TicketDAO");

    public DataBaseConfig dataBaseConfig = new DataBaseConfig();

    // Insère un nouveau ticket dans la base de données.
    public boolean saveTicket(Ticket ticket) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet generatedKeys = null;
        try {
            con = dataBaseConfig.getConnection();
            ps = con.prepareStatement(DBConstants.SAVE_TICKET, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, ticket.getParkingSpot().getId());
            ps.setString(2, ticket.getVehicleRegNumber());
            ps.setDouble(3, ticket.getPrice());
            ps.setTimestamp(4, new Timestamp(ticket.getInTime().getTime()));
            ps.setTimestamp(5, ticket.getOutTime() == null ? null : new Timestamp(ticket.getOutTime().getTime()));

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating ticket failed, no rows affected.");
            }

            generatedKeys = ps.getGeneratedKeys();
            if (generatedKeys.next()) {
                ticket.setId(generatedKeys.getInt(1)); // ✅ très important !
            }

            return true;
        } catch (Exception ex) {
            logger.error("Error saving ticket info", ex);
            return false;
        } finally {
            dataBaseConfig.closeResultSet(generatedKeys);
            dataBaseConfig.closePreparedStatement(ps);
            dataBaseConfig.closeConnection(con);
        }
    }


    //Récupère le dernier ticket (le plus récent) d’un véhicule donné.
    public Ticket getTicket(String vehicleRegNumber) {
        Connection con = null;
        Ticket ticket = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = dataBaseConfig.getConnection();
            ps = con.prepareStatement(
                "SELECT t.PARKING_NUMBER, t.ID, t.PRICE, t.IN_TIME, t.OUT_TIME, p.TYPE " +
                "FROM ticket t, parking p " +
                "WHERE p.parking_number = t.parking_number " +
                "AND t.VEHICLE_REG_NUMBER = ? " +
                "ORDER BY t.IN_TIME DESC " +  //  ticket le plus récent
                "LIMIT 1"
            );

            ps.setString(1, vehicleRegNumber);
            rs = ps.executeQuery();

            if (rs.next()) {
                ticket = new Ticket();
                ParkingSpot parkingSpot = new ParkingSpot(
                    rs.getInt(1), 
                    ParkingType.valueOf(rs.getString(6)), 
                    false
                );
                ticket.setParkingSpot(parkingSpot);
                ticket.setId(rs.getInt(2));
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setPrice(rs.getDouble(3));
                ticket.setInTime(rs.getTimestamp(4));
                ticket.setOutTime(rs.getTimestamp(5));
            }

        } catch (Exception ex) {
            logger.error("Error fetching latest ticket for vehicle: " + vehicleRegNumber, ex);
        } finally {
            dataBaseConfig.closeResultSet(rs);
            dataBaseConfig.closePreparedStatement(ps);
            dataBaseConfig.closeConnection(con);
        }

        return ticket;
    }


    //Met à jour le prix et le OUT_TIME d’un ticket existant (à la sortie du véhicule).
    public boolean updateTicket(Ticket ticket) {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = dataBaseConfig.getConnection();
            ps = con.prepareStatement(DBConstants.UPDATE_TICKET);
            ps.setDouble(1, ticket.getPrice());
            ps.setTimestamp(2, new Timestamp(ticket.getOutTime().getTime())); 
            ps.setInt(3, ticket.getId());

            return ps.executeUpdate() == 1;
        } catch (Exception ex) {
            logger.error("Error updating ticket info", ex);
            return false;
        } finally {
            dataBaseConfig.closePreparedStatement(ps);
            dataBaseConfig.closeConnection(con);
        }
    }

    
   
    //utile pour détecter les utilisateurs récurrents
    public int getNbTicket(String vehicleRegNumber) {
        int nbTicket = 0;
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = dataBaseConfig.getConnection();
            ps = con.prepareStatement("SELECT COUNT(*) FROM ticket WHERE VEHICLE_REG_NUMBER = ?");
            ps.setString(1, vehicleRegNumber);
            rs = ps.executeQuery();
            if (rs.next()) {
                nbTicket = rs.getInt(1);
            }
        } catch (Exception ex) {
            logger.error("Erreur dans getNbTicket", ex);
        } finally {
            dataBaseConfig.closeResultSet(rs);
            dataBaseConfig.closePreparedStatement(ps);
            dataBaseConfig.closeConnection(con);
        }
        return nbTicket;
    }
    
    
    //Même but que getTicket, mais utilise une jointure explicite JOIN (même effet, légèrement différente).
    public Ticket getLatestTicket(String vehicleRegNumber) {
        Connection con = null;
        Ticket ticket = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = dataBaseConfig.getConnection();
            ps = con.prepareStatement(
                "SELECT t.PARKING_NUMBER, t.ID, t.PRICE, t.IN_TIME, t.OUT_TIME, p.TYPE " +
                "FROM ticket t JOIN parking p ON t.PARKING_NUMBER = p.PARKING_NUMBER " +
                "WHERE t.VEHICLE_REG_NUMBER = ? ORDER BY t.IN_TIME DESC LIMIT 1"
            );
            ps.setString(1, vehicleRegNumber);
            rs = ps.executeQuery();
            if (rs.next()) {
                ticket = new Ticket();
                ParkingSpot parkingSpot = new ParkingSpot(rs.getInt(1), ParkingType.valueOf(rs.getString(6)), false);
                ticket.setParkingSpot(parkingSpot);
                ticket.setId(rs.getInt(2));
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setPrice(rs.getDouble(3));
                ticket.setInTime(rs.getTimestamp(4));
                ticket.setOutTime(rs.getTimestamp(5));
            }
        } catch (Exception ex) {
            logger.error("Erreur dans getLatestTicket", ex);
        } finally {
            dataBaseConfig.closeResultSet(rs);
            dataBaseConfig.closePreparedStatement(ps);
            dataBaseConfig.closeConnection(con);
        }
        return ticket;
    }
    
    
    //Met à jour seulement le champ IN_TIME d’un ticket identifié par son ID
    public boolean updateInTime(Ticket ticket) {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = dataBaseConfig.getConnection();
            ps = con.prepareStatement("UPDATE ticket SET IN_TIME=? WHERE ID=?");
            ps.setTimestamp(1, new Timestamp(ticket.getInTime().getTime()));
            ps.setInt(2, ticket.getId());
            return ps.executeUpdate() == 1;
        } catch (Exception ex) {
            logger.error("Error updating inTime", ex);
            return false;
        } finally {
            dataBaseConfig.closePreparedStatement(ps);
            dataBaseConfig.closeConnection(con);
        }
    }



}
