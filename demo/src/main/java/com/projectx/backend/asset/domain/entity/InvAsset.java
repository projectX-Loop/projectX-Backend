package com.projectx.backend.asset.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inv_asset")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InvAsset {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "code", nullable = false, unique = true)
	private String code;

	@Column(name = "instrument", nullable = false)
	private String instrument;

	@Column(name = "display_name", nullable = false)
	private String displayName;

	@Column(name = "currency_code", nullable = false)
	private String currencyCode;

	@Column(name = "tax_class", nullable = false)
	private String taxClass;

	public static InvAsset create(String code, String instrument, String displayName, String currencyCode,
			String taxClass) {
		InvAsset invAsset = new InvAsset();
		invAsset.code = code;
		invAsset.instrument = instrument;
		invAsset.displayName = displayName;
		invAsset.currencyCode = currencyCode;
		invAsset.taxClass = taxClass;
		return invAsset;
	}

}
