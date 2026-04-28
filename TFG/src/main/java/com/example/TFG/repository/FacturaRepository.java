package com.example.TFG.repository;

import com.example.TFG.model.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FacturaRepository extends JpaRepository<Factura, Integer> {

    @Query("SELECT f FROM Factura f WHERE f.empresa.id_empresa = :idEmpresa")
    List<Factura> findFacturasByEmpresaId(@Param("idEmpresa") Long idEmpresa);
}
