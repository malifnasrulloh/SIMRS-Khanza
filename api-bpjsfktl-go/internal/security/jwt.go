package security

import (
	"fmt"
	"time"

	"github.com/golang-jwt/jwt/v5"
)

type Claims struct {
	Data struct {
		Username string `json:"username"`
	} `json:"data"`
	jwt.RegisteredClaims
}

func GenerateToken(username, secret string, expSeconds int) (string, error) {
	claims := Claims{
		Data: struct {
			Username string `json:"username"`
		}{
			Username: username,
		},
		RegisteredClaims: jwt.RegisteredClaims{
			Issuer:    "Khanza REST API",
			Audience:  jwt.ClaimStrings{"Client Khanza REST API"},
			IssuedAt:  jwt.NewNumericDate(time.Now()),
			ExpiresAt: jwt.NewNumericDate(time.Now().Add(time.Duration(expSeconds) * time.Second)),
		},
	}

	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	tokenString, err := token.SignedString([]byte(secret))
	if err != nil {
		return "", fmt.Errorf("sign jwt token: %w", err)
	}
	return tokenString, nil
}

func ValidateToken(tokenStr, secret string) (bool, error) {
	token, err := jwt.ParseWithClaims(tokenStr, &Claims{}, func(token *jwt.Token) (any, error) {
		if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, fmt.Errorf("unexpected signing method: %v", token.Header["alg"])
		}
		return []byte(secret), nil
	})

	if err != nil || !token.Valid {
		return false, err
	}
	return true, nil
}
