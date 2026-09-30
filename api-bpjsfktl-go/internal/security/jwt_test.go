package security

import (
	"testing"
)

func TestJWTGenerationAndValidation(t *testing.T) {
	secret := "123!!abc**"
	token, err := GenerateToken("admin", secret, 3600)
	if err != nil {
		t.Fatalf("token generation failed: %v", err)
	}

	valid, err := ValidateToken(token, secret)
	if err != nil || !valid {
		t.Errorf("token validation failed: %v", err)
	}

	// Tampered token test
	tampered := token + "bad"
	valid, _ = ValidateToken(tampered, secret)
	if valid {
		t.Errorf("expected tampered token to fail validation")
	}

	// Wrong secret test
	valid, _ = ValidateToken(token, "wrongsecret")
	if valid {
		t.Errorf("expected token to fail with wrong secret")
	}
}
