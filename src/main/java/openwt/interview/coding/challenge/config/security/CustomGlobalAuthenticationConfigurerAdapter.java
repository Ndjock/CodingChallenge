package openwt.interview.coding.challenge.config.security;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import openwt.interview.coding.challenge.persistence.entities.Contact;
import openwt.interview.coding.challenge.persistence.repos.ContactRepository;

@Configuration
class CustomGlobalAuthenticationConfigurerAdapter extends GlobalAuthenticationConfigurerAdapter {

  @Autowired
  ContactRepository contactRepository;

  @Override
  public void init(AuthenticationManagerBuilder auth) throws Exception {
    auth.authenticationProvider(authenticationProvider());
    auth.eraseCredentials(false);
  }
  
  
  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
      DaoAuthenticationProvider authProvider
        = new DaoAuthenticationProvider();
      authProvider.setUserDetailsService(userDetailsService());
      authProvider.setPasswordEncoder(bCryptPasswordEncoder());
      return authProvider;
  }
  
  @Bean
  UserDetailsService userDetailsService() {
    return new UserDetailsService() {

      @Override
      public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Contact> contact = contactRepository.findByEmail(username);
//        if(contact.isPresent()) {
//        return new User(contact.get().getEmail(), bCryptPasswordEncoder().encode(contact.get().getEmail()), true, true, true, true,
//                AuthorityUtils.createAuthorityList("ROLE_USER"));
//        } else {
//          throw new UsernameNotFoundException("could not find the user '"
//                  + username + "'");
//        }
        
        if(contact.isPresent()) {
        	String password = bCryptPasswordEncoder().encode(contact.get().getEmail());
        	return User
        			.withUsername(contact.get().getEmail())
        			.password(password)
        			.authorities(AuthorityUtils.createAuthorityList("ROLE_USER"))
        			.build();
        }
        else 
        	throw new UsernameNotFoundException("could not find the user '"
                  + username + "'");
      }
      
    };
  }
  
  @Bean
  public BCryptPasswordEncoder bCryptPasswordEncoder() {
	    return new BCryptPasswordEncoder(11);
	}

}