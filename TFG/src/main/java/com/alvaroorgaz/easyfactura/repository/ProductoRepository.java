package com.alvaroorgaz.easyfactura.repository;

import com.alvaroorgaz.easyfactura.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    @Query("SELECT p FROM Producto p WHERE p.empresa.id_empresa = :idEmpresa")
    List<Producto> findProductosByEmpresaId(@Param("idEmpresa") Long idEmpresa);
}
