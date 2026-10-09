package com.example.pharmacy.service;

import com.example.pharmacy.entity.Medicine;
import com.example.pharmacy.repository.MedicineRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicineService {

    private final MedicineRepository medicineRepository;

    public MedicineService(MedicineRepository medicineRepository) {
        this.medicineRepository = medicineRepository;
    }

    public Medicine addMedicine(Medicine medicine) {
        return medicineRepository.save(medicine);
    }

    public List<Medicine> getAllMedicines() {
        return medicineRepository.findAll();
    }
    public List<Medicine> searchMedicines(String name) {
    return medicineRepository.findByNameContainingIgnoreCase(name);
    }

    public Medicine getMedicineById(Long id) {
        return medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicine not found"));
    }

    public Medicine updateMedicine(Long id, Medicine medicine) {

        Medicine existingMedicine = medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicine not found"));

        existingMedicine.setName(medicine.getName());
        existingMedicine.setCategory(medicine.getCategory());
        existingMedicine.setDescription(medicine.getDescription());
        existingMedicine.setPrice(medicine.getPrice());
        existingMedicine.setQuantity(medicine.getQuantity());
        existingMedicine.setExpiryDate(medicine.getExpiryDate());
        existingMedicine.setImageUrl(medicine.getImageUrl());
        existingMedicine.setPackageSize(medicine.getPackageSize());

        return medicineRepository.save(existingMedicine);
    }

    public String deleteMedicine(Long id) {

        if (!medicineRepository.existsById(id)) {
            return "Medicine not found";
        }

        medicineRepository.deleteById(id);

        return "Medicine deleted successfully";
    }
}