package com.ecommerce.service;
import org.junit.jupiter.api.Test; import static org.assertj.core.api.Assertions.assertThat;
class SlugServiceTest { private final SlugService slugService=new SlugService(); @Test void slugifiesCategoryNames(){ assertThat(slugService.slugify("Smart Home Devices!")).isEqualTo("smart-home-devices"); } }