package com.alvaroorgaz.easyfactura.repository;

import com.alvaroorgaz.easyfactura.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    @Query("SELECT c FROM Cliente c WHERE c.empresa.idEmpresa = :idEmpresa")
    List<Cliente> findClientesByEmpresaId(@Param("idEmpresa") Integer idEmpresa);
}
