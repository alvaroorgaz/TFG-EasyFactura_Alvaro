package com.example.TFG.repository;

import com.example.TFG.model.FacturaRectificada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FacturaRectificadaRepository extends JpaRepository<FacturaRectificada, Integer> {

    @Query("SELECT fr FROM FacturaRectificada fr WHERE fr.facturaOriginal.idFactura = :idFactura ORDER BY fr.fecha ASC")
    List<FacturaRectificada> findByFacturaOriginalId(@Param("idFactura") Integer idFactura);

    @Query("SELECT fr FROM FacturaRectificada fr WHERE fr.facturaRectificada.idFactura = :idFactura")
    FacturaRectificada findByFacturaRectificadaId(@Param("idFactura") Integer idFactura);
}
