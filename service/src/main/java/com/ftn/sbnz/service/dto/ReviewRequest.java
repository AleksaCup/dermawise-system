package com.ftn.sbnz.service.dto;

import jakarta.validation.constraints.Size;

public record ReviewRequest(@Size(max = 1000) String comment) {
}
