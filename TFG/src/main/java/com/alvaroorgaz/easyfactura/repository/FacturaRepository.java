package com.alvaroorgaz.easyfactura.repository;

import com.alvaroorgaz.easyfactura.model.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FacturaRepository extends JpaRepository<Factura, Integer> {

    @Query("SELECT f FROM Factura f WHERE f.empresa.idEmpresa = :idEmpresa")
    List<Factura> findFacturasByEmpresaId(@Param("idEmpresa") Integer idEmpresa);

    @Query(value = "SELECT * FROM factura WHERE id_empresa = :idEmpresa AND id_factura < :idFactura ORDER BY id_factura DESC LIMIT 1", nativeQuery = true)
    Factura findUltimaFacturaAnterior(@Param("idEmpresa") Integer idEmpresa, @Param("idFactura") Integer idFactura);
}
