package com.winnie.app.model.entity;

import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.util.Date;

public class Billing {

    private Long TenantId;

    private String billNo;

    private Double HouseCharge;

    private Double otherCharge;


    private Date paymentDate;
}
