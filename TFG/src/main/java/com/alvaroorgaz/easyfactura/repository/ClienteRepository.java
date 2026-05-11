package com.alvaroorgaz.easyfactura.repository;

import com.alvaroorgaz.easyfactura.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    @Query("SELECT c FROM Cliente c WHERE c.empresa.id_empresa = :idEmpresa")
    List<Cliente> findClientesByEmpresaId(@Param("idEmpresa") Long idEmpresa);
}
