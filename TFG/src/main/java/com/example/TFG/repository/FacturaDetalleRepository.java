package com.example.TFG.repository;

import com.example.TFG.model.FacturaDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FacturaDetalleRepository extends JpaRepository<FacturaDetalle, Integer> {

    @Query("SELECT fd FROM FacturaDetalle fd WHERE fd.factura.idFactura = :idFactura")
    List<FacturaDetalle> findByFacturaId(@Param("idFactura") Integer idFactura);

    @Modifying
    @Query("DELETE FROM FacturaDetalle fd WHERE fd.factura.idFactura = :idFactura")
    void deleteByFacturaId(@Param("idFactura") Integer idFactura);
}
