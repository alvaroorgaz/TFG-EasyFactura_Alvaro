package com.alvaroorgaz.easyfactura.repository;

import com.alvaroorgaz.easyfactura.model.FacturaRectificada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FacturaRectificadaRepository extends JpaRepository<FacturaRectificada, Long> {

    @Query("SELECT fr FROM FacturaRectificada fr WHERE fr.facturaOriginal.idFactura = :idFactura ORDER BY fr.fecha ASC")
    List<FacturaRectificada> findByFacturaOriginalId(@Param("idFactura") Long idFactura);

    @Query("SELECT fr FROM FacturaRectificada fr WHERE fr.facturaRectificada.idFactura = :idFactura")
    FacturaRectificada findByFacturaRectificadaId(@Param("idFactura") Long idFactura);
}
