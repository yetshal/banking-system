package com.banking.system.service;

import com.banking.system.entity.Cliente;
import com.banking.system.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de Clientes Contiene la lógica de negocio y validaciones
 */
@Service
@Transactional  // Asegura que las operaciones de BD sean atómicas
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    /**
     * Crear un nuevo cliente Validaciones: - No puede ser menor de edad - No
     * puede existir con el mismo número de identificación - No puede existir
     * con el mismo correo
     */
    public Cliente crearCliente(Cliente cliente) {
        // Validar que sea mayor de edad
        if (!cliente.esMayorDeEdad()) {
            throw new IllegalArgumentException(
                    "El cliente debe ser mayor de edad. Edad actual: " + cliente.calcularEdad() + " años"
            );
        }

        // Validar que no exista el número de identificación
        if (clienteRepository.existsByNumeroIdentificacion(cliente.getNumeroIdentificacion())) {
            throw new IllegalArgumentException(
                    "Ya existe un cliente con el número de identificación: " + cliente.getNumeroIdentificacion()
            );
        }

        // Validar que no exista el correo
        if (clienteRepository.findByCorreoElectronico(cliente.getCorreoElectronico()).isPresent()) {
            throw new IllegalArgumentException(
                    "Ya existe un cliente con el correo electrónico: " + cliente.getCorreoElectronico()
            );
        }

        return clienteRepository.save(cliente);
    }

    /**
     * Actualizar un cliente existente
     */
    public Cliente actualizarCliente(Long id, Cliente clienteActualizado) {
        // Verificar que el cliente existe
        Cliente clienteExistente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + id));

        // Validar que sea mayor de edad
        if (!clienteActualizado.esMayorDeEdad()) {
            throw new IllegalArgumentException(
                    "El cliente debe ser mayor de edad. Edad actual: " + clienteActualizado.calcularEdad() + " años"
            );
        }

        // Validar que el correo no esté en uso por otro cliente
        if (clienteRepository.existsByCorreoElectronicoAndIdNot(
                clienteActualizado.getCorreoElectronico(), id)) {
            throw new IllegalArgumentException(
                    "El correo electrónico ya está en uso por otro cliente"
            );
        }

        // Validar que el número de identificación no esté en uso por otro cliente
        if (clienteRepository.existsByNumeroIdentificacionAndIdNot(
                clienteActualizado.getNumeroIdentificacion(), id)) {
            throw new IllegalArgumentException(
                    "El número de identificación ya está en uso por otro cliente"
            );
        }

        // Actualizar los campos
        clienteExistente.setTipoIdentificacion(clienteActualizado.getTipoIdentificacion());
        clienteExistente.setNumeroIdentificacion(clienteActualizado.getNumeroIdentificacion());
        clienteExistente.setNombres(clienteActualizado.getNombres());
        clienteExistente.setApellido(clienteActualizado.getApellido());
        clienteExistente.setCorreoElectronico(clienteActualizado.getCorreoElectronico());
        clienteExistente.setFechaNacimiento(clienteActualizado.getFechaNacimiento());

        // La fecha de modificación se actualiza automáticamente con @UpdateTimestamp
        return clienteRepository.save(clienteExistente);
    }

    /**
     * Obtener todos los clientes
     */
    public List<Cliente> obtenerTodosLosClientes() {
        return clienteRepository.findAll();
    }

    /**
     * Obtener un cliente por ID
     */
    public Cliente obtenerClientePorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + id));
    }

    /**
     * Eliminar un cliente Validación: No puede tener productos vinculados
     */
    public void eliminarCliente(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + id));

        // Validar que no tenga cuentas vinculadas
        if (cliente.tieneCuentasVinculadas()) {
            throw new IllegalArgumentException(
                    "No se puede eliminar el cliente porque tiene "
                    + cliente.getCuentas().size() + " cuenta(s) vinculada(s)"
            );
        }

        clienteRepository.delete(cliente);
    }
}
