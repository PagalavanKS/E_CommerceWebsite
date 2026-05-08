package com.ecommerce.service;
import org.springframework.stereotype.Service;
@Service public class SlugService { public String slugify(String value){ return value.toLowerCase().replaceAll("[^a-z0-9]+","-").replaceAll("(^-|-$)",""); } }