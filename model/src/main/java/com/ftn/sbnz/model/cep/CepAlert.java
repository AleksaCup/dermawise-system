package com.ftn.sbnz.model.cep;

import com.ftn.sbnz.model.enums.CepAlertType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CepAlert {

    private String userId;
    private CepAlertType type;
    private String message;
    private Date timestamp;
}
